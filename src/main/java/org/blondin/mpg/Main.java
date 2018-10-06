package org.blondin.mpg;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        process(mpgClient, mpgStatsClient);
    }

    static void process(MpgClient mpgClient, MpgStatsClient mpgStatsClient) {
        for (League league : mpgClient.getDashboard().getLeagues()) {
            LOG.info("========== {} ==========", league.getName());

            List<Player> players = calculateEfficiency(mpgClient.getCoach(league.getId()).getPlayers(), mpgStatsClient);
            Collections.sort(players, Comparator.comparing(Player::getPosition).thenComparing(Player::getEfficiency).reversed());
            int nameMaxLength = players.stream().map(Player::getName).collect(Collectors.toList()).stream().max(Comparator.comparing(String::length))
                    .orElse("").length();
            Position lp = Position.G;
            for (Player player : players) {

                // Write position separator
                if (!player.getPosition().equals(lp)) {
                    lp = player.getPosition();
                    String dashes = IntStream.range(0, nameMaxLength + 11).mapToObj(i -> "-").collect(Collectors.joining(""));
                    LOG.info("{}", dashes);
                }

                // Write player
                String spacesAfterName = IntStream.range(0, nameMaxLength - player.getName().length()).mapToObj(i -> " ")
                        .collect(Collectors.joining(""));
                LOG.info("{} | {}{} | {}", player.getPosition(), player.getName(), spacesAfterName, player.getEfficiency());
            }
        }
    }

    static List<Player> calculateEfficiency(List<Player> players, MpgStatsClient stats) {
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
