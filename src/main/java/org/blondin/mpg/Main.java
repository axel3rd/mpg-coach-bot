package org.blondin.mpg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.blondin.mpg.equipeactu.InjuredSuspendedClient;
import org.blondin.mpg.equipeactu.model.OutType;
import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.root.model.Position;
import org.blondin.mpg.stats.MpgStatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String configFile = null;
        if (args != null && args.length > 0) {
            configFile = args[0];
        }
        Config config = Config.build(configFile);
        MpgClient mpgClient = MpgClient.build(config.getLogin(), config.getPassword());
        MpgStatsClient mpgStatsClient = MpgStatsClient.build();
        InjuredSuspendedClient outPlayersClient = InjuredSuspendedClient.build();
        process(mpgClient, mpgStatsClient, outPlayersClient);
    }

    static void process(MpgClient mpgClient, MpgStatsClient mpgStatsClient, InjuredSuspendedClient outPlayersClient) {
        for (League league : mpgClient.getDashboard().getLeagues()) {
            LOG.info("========== {} ==========", league.getName());

            // Get players
            List<Player> players = mpgClient.getCoach(league.getId()).getPlayers();

            // Remove out players (and write them)
            removeOutPlayers(players, outPlayersClient);

            // Calculate efficiency and sort
            calculateEfficiency(players, mpgStatsClient);
            Collections.sort(players, Comparator.comparing(Player::getPosition).thenComparing(Player::getEfficiency).reversed());

            // Write optimized team
            writeTeamOptimized(players);
        }
    }

    static List<Player> removeOutPlayers(List<Player> players, InjuredSuspendedClient outPlayersClient) {
        List<Player> outPlayers = new ArrayList<>();
        for (Player player : players) {
            org.blondin.mpg.equipeactu.model.Player outPlayer = outPlayersClient.getPlayer(player.getName(), OutType.INJURY_GREEN);
            if (outPlayer != null) {
                outPlayers.add(player);
                LOG.info("Out: {} - {} - {} - {}", player.getName(), outPlayer.getOutType(), outPlayer.getDescription(), outPlayer.getLength());
            }
        }
        players.removeAll(outPlayers);
        return players;
    }

    private static void writeTeamOptimized(List<Player> players) {
        final int nameMaxLength = players.stream().map(Player::getName).collect(Collectors.toList()).stream()
                .max(Comparator.comparing(String::length)).orElse("").length();
        final String dashes = IntStream.range(0, nameMaxLength + 11).mapToObj(i -> "-").collect(Collectors.joining(""));

        LOG.info("\nOptimized team:\n{}", dashes);
        Position lp = Position.G;
        for (Player player : players) {

            // Write position separator
            if (!player.getPosition().equals(lp)) {
                lp = player.getPosition();
                LOG.info("{}", dashes);
            }

            // Write player
            String spacesAfterName = IntStream.range(0, nameMaxLength - player.getName().length()).mapToObj(i -> " ").collect(Collectors.joining(""));
            LOG.info("{} | {}{} | {}", player.getPosition(), player.getName(), spacesAfterName, player.getEfficiency());
        }
        LOG.info("{}", dashes);
    }

    private static List<Player> calculateEfficiency(List<Player> players, MpgStatsClient stats) {
        // Calculate efficient in Stats model
        for (org.blondin.mpg.stats.model.Player p : stats.getStats().getPlayers()) {
            double efficiency = p.getStats().getMatchs() / (double) stats.getStats().getDay() * p.getStats().getAverage()
                    * (1 + p.getStats().getGoals() * 1.2);
            // round efficiency to 2 decimals
            p.setEfficiency(Math.round(efficiency * 100) / (double) 100);
        }

        // Fill MPG model
        for (Player player : players) {
            player.setEfficiency(stats.getStats().getPlayer(player.getName()).getEfficiency());
        }
        return players;
    }

}
