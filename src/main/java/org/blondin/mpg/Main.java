package org.blondin.mpg;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.equipeactu.ChampionshipOutType;
import org.blondin.mpg.equipeactu.InjuredSuspendedClient;
import org.blondin.mpg.equipeactu.model.OutType;
import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.root.exception.NoMoreGamesException;
import org.blondin.mpg.root.exception.PlayerNotFoundException;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.CoachRequest;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.LeagueStatus;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.root.model.Position;
import org.blondin.mpg.root.model.TacticalSubstitute;
import org.blondin.mpg.root.model.TransferBuy;
import org.blondin.mpg.stats.ChampionshipStatsType;
import org.blondin.mpg.stats.MpgStatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vandermeer.asciitable.AT_Cell;
import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.asciithemes.a7.A7_Grids;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

public class Main {

    private static final DecimalFormat FORMAT_DECIMAL_DOUBLE = new DecimalFormat("0.00");

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String configFile = null;
        if (args != null && args.length > 0) {
            configFile = args[0];
        }
        Config config = Config.build(configFile);
        MpgClient mpgClient = MpgClient.build(config);
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(config);
        InjuredSuspendedClient outPlayersClient = InjuredSuspendedClient.build(config);
        process(mpgClient, mpgStatsClient, outPlayersClient, config);
    }

    static void process(MpgClient mpgClient, MpgStatsClient mpgStatsClient, InjuredSuspendedClient outPlayersClient, Config config) {
        for (League league : mpgClient.getDashboard().getLeagues()) {
            if (LeagueStatus.TERMINATED.equals(league.getLeagueStatus())) {
                // Don't display any logs
                continue;
            }
            processLeague(league, mpgClient, mpgStatsClient, outPlayersClient, config);
        }
    }

    static void processLeague(League league, MpgClient mpgClient, MpgStatsClient mpgStatsClient, InjuredSuspendedClient outPlayersClient,
            Config config) {
        LOG.info("========== {} ==========", league.getName());
        switch (league.getLeagueStatus()) {
        case TERMINATED:
            // Already managed previously
        case CREATION:
        case UNKNOWN:
            processMercatoChampionship(league, mpgClient, mpgStatsClient, outPlayersClient, config);
            break;
        case MERCATO:
            if (league.getTeamStatus() == 1) {
                LOG.info("\nMercato turn is closed, come back for the next !\n");
                return;
            }
            processMercatoLeague(league, mpgClient, mpgStatsClient, outPlayersClient, config);
            break;
        case GAMES:
            processGames(league, mpgClient, mpgStatsClient, outPlayersClient, config);
            break;
        }
    }

    static void processMercatoLeague(League league, MpgClient mpgClient, MpgStatsClient mpgStatsClient, InjuredSuspendedClient outPlayersClient,
            Config config) {
        LOG.info("\nProposal for your mercato:\n");
        List<Player> players = mpgClient.getMercato(league.getId()).getPlayers();
        calculateEfficiency(players, mpgStatsClient, ChampionshipTypeWrapper.toStats(league.getChampionship()), config, false);
        processMercato(players, outPlayersClient, ChampionshipTypeWrapper.toOut(league.getChampionship()));
    }

    static void processMercatoChampionship(League league, MpgClient mpgClient, MpgStatsClient mpgStatsClient, InjuredSuspendedClient outPlayersClient,
            Config config) {
        LOG.info("\nProposal for your coming soon mercato:\n");
        List<Player> players = mpgClient.getMercato(league.getChampionship()).getPlayers();
        calculateEfficiency(players, mpgStatsClient, ChampionshipTypeWrapper.toStats(league.getChampionship()), config, false);
        processMercato(players, outPlayersClient, ChampionshipTypeWrapper.toOut(league.getChampionship()));
    }

    static void processMercato(List<Player> players, InjuredSuspendedClient outPlayersClient, ChampionshipOutType championship) {
        Collections.sort(players, Comparator.comparing(Player::getPosition).thenComparing(Player::getEfficiency).reversed());
        List<Player> goals = players.stream().filter(p -> p.getPosition().equals(Position.G)).collect(Collectors.toList()).subList(0, 5);
        List<Player> defenders = players.stream().filter(p -> p.getPosition().equals(Position.D)).collect(Collectors.toList()).subList(0, 10);
        List<Player> midfielders = players.stream().filter(p -> p.getPosition().equals(Position.M)).collect(Collectors.toList()).subList(0, 10);
        List<Player> attackers = players.stream().filter(p -> p.getPosition().equals(Position.A)).collect(Collectors.toList()).subList(0, 10);

        AsciiTable at = getTable("P", "Player name", "Q.", "Eff.", "Out info");
        for (List<Player> line : Arrays.asList(goals, defenders, midfielders, attackers)) {
            for (Player player : line) {
                org.blondin.mpg.equipeactu.model.Player outPlayer = outPlayersClient.getPlayer(championship, player.getName(),
                        PositionWrapper.toOut(player.getPosition()), OutType.INJURY_GREEN);
                String outInfos = "";
                if (outPlayer != null) {
                    outInfos = String.format("%s - %s - %s", outPlayer.getOutType(), outPlayer.getDescription(), outPlayer.getLength());
                }
                AT_Row row = at.addRow(player.getPosition(), player.getName(), player.getQuotation(),
                        FORMAT_DECIMAL_DOUBLE.format(player.getEfficiency()), outInfos);
                setTableFormatRowPaddingSpace(row);
                row.getCells().get(2).getContext().setTextAlignment(TextAlignment.RIGHT);
                row.getCells().get(3).getContext().setTextAlignment(TextAlignment.RIGHT);
            }
            at.addRule();
        }

        String render = at.render();
        LOG.info(render);
        LOG.info("");
    }

    static void processGames(League league, MpgClient mpgClient, MpgStatsClient mpgStatsClient, InjuredSuspendedClient outPlayersClient,
            Config config) {
        try {

            // Get players
            Coach coach = mpgClient.getCoach(league.getId());
            List<Player> players = coach.getPlayers();

            // Calculate efficiency (notes should be in injured players display), and save for transactions proposal
            calculateEfficiency(players, mpgStatsClient, ChampionshipTypeWrapper.toStats(league.getChampionship()), config, true);
            List<Player> playersTeam = players.stream().collect(Collectors.toList());

            // Remove out players (and write them)
            removeOutPlayers(players, outPlayersClient, ChampionshipTypeWrapper.toOut(league.getChampionship()));

            // Sort by efficiency
            Collections.sort(players, Comparator.comparing(Player::getPosition).thenComparing(Player::getEfficiency).reversed());

            // Write optimized team
            writeTeamOptimized(players);

            // Auto-update team
            if (config.isTeampUpdate()) {
                LOG.info("\nUpdating team ...");
                mpgClient.updateCoach(league, getCoachRequest(coach, players, config));
            }

            if (config.isTransactionsProposal()) {
                LOG.info("\nTransactions proposal ...");
                TransferBuy transferBuy = mpgClient.getTransferBuy(league.getId());
                List<Player> playersAvailable = transferBuy.getAvailablePlayers();
                calculateEfficiency(playersAvailable, mpgStatsClient, ChampionshipTypeWrapper.toStats(league.getChampionship()), config, true);
                writeTransactionsProposal(playersTeam, playersAvailable, transferBuy.getBudget(), outPlayersClient,
                        ChampionshipTypeWrapper.toOut(league.getChampionship()), config);
            }
        } catch (NoMoreGamesException e) {
            LOG.info("\nNo more games in this league ...\n");
        }
        LOG.info("");
    }

    private static void writeTransactionsProposal(List<Player> playersTeam, List<Player> playersAvailable, int budget,
            InjuredSuspendedClient outPlayersClient, ChampionshipOutType championship, Config config) {

        // Players with bad efficiency
        List<Player> players2Sell = playersTeam.stream().filter(p -> p.getEfficiency() <= config.getEfficiencySell(p.getPosition()))
                .collect(Collectors.toList());

        // Remove goals if same team as the first
        Player goalFirst = playersTeam.stream().filter(p -> p.getPosition().equals(Position.G))
                .sorted(Comparator.comparing(Player::getEfficiency).reversed()).collect(Collectors.toList()).get(0);
        players2Sell.removeIf(p -> p.getPosition().equals(Position.G) && p.getTeamId() == goalFirst.getTeamId());

        int cash = budget;
        if (!players2Sell.isEmpty()) {
            LOG.info("Players to sell (initial cash: {}):", budget);
            AsciiTable at = getTable("P", "Player name", "Eff.", "Quote");
            for (Player player : players2Sell) {
                cash += player.getQuotation();
                AT_Row row = at.addRow(player.getPosition(), player.getName(), FORMAT_DECIMAL_DOUBLE.format(player.getEfficiency()),
                        player.getQuotation());
                setTableFormatRowPaddingSpace(row);
                row.getCells().get(2).getContext().setTextAlignment(TextAlignment.RIGHT);
            }
            at.addRule();
            String render = at.render();
            LOG.info(render);
        }
        LOG.info("Budget: {}", cash);

        Player defenderLast = playersTeam.stream().filter(p -> p.getPosition().equals(Position.D)).sorted(Comparator.comparing(Player::getEfficiency))
                .collect(Collectors.toList()).get(0);
        Player midfielderLast = playersTeam.stream().filter(p -> p.getPosition().equals(Position.M))
                .sorted(Comparator.comparing(Player::getEfficiency)).collect(Collectors.toList()).get(0);
        Player attackerLast = playersTeam.stream().filter(p -> p.getPosition().equals(Position.A)).sorted(Comparator.comparing(Player::getEfficiency))
                .collect(Collectors.toList()).get(0);
        cash += goalFirst.getQuotation() + defenderLast.getQuotation() + midfielderLast.getQuotation() + attackerLast.getQuotation();
        LOG.info("Budget if last players by line sold: {}", cash);

        final int budgetPotential = cash;
        List<Player> players2buy = new ArrayList<>();
        players2buy.addAll(playersAvailable.stream().filter(p -> p.getPosition().equals(Position.G)).filter(p -> p.getQuotation() <= budgetPotential)
                .filter(p -> p.getEfficiency() > goalFirst.getEfficiency()).filter(p -> p.getEfficiency() > config.getEfficiencySell(Position.G))
                .sorted(Comparator.comparing(Player::getEfficiency).reversed()).limit(3).collect(Collectors.toList()));
        players2buy.addAll(playersAvailable.stream().filter(p -> p.getPosition().equals(Position.D)).filter(p -> p.getQuotation() <= budgetPotential)
                .filter(p -> p.getEfficiency() > defenderLast.getEfficiency()).filter(p -> p.getEfficiency() > config.getEfficiencySell(Position.D))
                .sorted(Comparator.comparing(Player::getEfficiency).reversed()).limit(3).collect(Collectors.toList()));
        players2buy.addAll(playersAvailable.stream().filter(p -> p.getPosition().equals(Position.M)).filter(p -> p.getQuotation() <= budgetPotential)
                .filter(p -> p.getEfficiency() > midfielderLast.getEfficiency()).filter(p -> p.getEfficiency() > config.getEfficiencySell(Position.M))
                .sorted(Comparator.comparing(Player::getEfficiency).reversed()).limit(3).collect(Collectors.toList()));
        players2buy.addAll(playersAvailable.stream().filter(p -> p.getPosition().equals(Position.A)).filter(p -> p.getQuotation() <= budgetPotential)
                .filter(p -> p.getEfficiency() > attackerLast.getEfficiency()).filter(p -> p.getEfficiency() > config.getEfficiencySell(Position.A))
                .sorted(Comparator.comparing(Player::getEfficiency).reversed()).limit(3).collect(Collectors.toList()));

        if (!players2buy.isEmpty()) {
            LOG.info("Player(s) to buy (3 best choice by line):");
            AsciiTable at = getTable("P", "Player name", "Eff.", "Price");
            for (Player player : players2buy) {
                org.blondin.mpg.equipeactu.model.Player outPlayer = outPlayersClient.getPlayer(championship, player.getName(),
                        PositionWrapper.toOut(player.getPosition()), OutType.INJURY_GREEN);
                String s = player.getName();
                if (outPlayer != null) {
                    s += String.format(" (%s - %s - %s)", outPlayer.getOutType(), outPlayer.getDescription(), outPlayer.getLength());
                }
                AT_Row row = at.addRow(player.getPosition(), s, FORMAT_DECIMAL_DOUBLE.format(player.getEfficiency()), player.getQuotation());
                setTableFormatRowPaddingSpace(row);
                row.getCells().get(2).getContext().setTextAlignment(TextAlignment.RIGHT);
            }
            at.addRule();
            String render = at.render();
            LOG.info(render);
        } else {
            LOG.info("No better players to buy, sorry.");
        }

    }

    static List<Player> removeOutPlayers(List<Player> players, InjuredSuspendedClient outPlayersClient, ChampionshipOutType championship) {
        List<Player> outPlayers = new ArrayList<>();
        for (Player player : players) {
            org.blondin.mpg.equipeactu.model.Player outPlayer = outPlayersClient.getPlayer(championship, player.getName(),
                    PositionWrapper.toOut(player.getPosition()), OutType.INJURY_GREEN);
            if (outPlayer != null) {
                outPlayers.add(player);
                String eff = FORMAT_DECIMAL_DOUBLE.format(player.getEfficiency());
                LOG.info("Out: {} ({} - {}) - {} - {} - {}", player.getName(), player.getPosition(), eff, outPlayer.getOutType(),
                        outPlayer.getDescription(), outPlayer.getLength());
            }
        }
        players.removeAll(outPlayers);
        return players;
    }

    private static void writeTeamOptimized(List<Player> players) {
        LOG.info("\nOptimized team:");
        AsciiTable at = getTable("P", "Player name", "Eff.", "Quote");
        Position lp = Position.G;
        for (Player player : players) {
            // Write position separator
            if (!player.getPosition().equals(lp)) {
                lp = player.getPosition();
                at.addRule();
            }
            AT_Row row = at.addRow(player.getPosition(), player.getName(), FORMAT_DECIMAL_DOUBLE.format(player.getEfficiency()),
                    player.getQuotation());
            setTableFormatRowPaddingSpace(row);
            row.getCells().get(2).getContext().setTextAlignment(TextAlignment.RIGHT);
        }
        at.addRule();
        String render = at.render();
        LOG.info(render);
    }

    private static List<Player> calculateEfficiency(List<Player> players, MpgStatsClient stats, ChampionshipStatsType championship, Config config,
            boolean failIfPlayerNotFound) {
        // Calculate efficient in Stats model
        for (org.blondin.mpg.stats.model.Player p : stats.getStats(championship).getPlayers()) {
            double efficiency = p.getStats().getMatchs() / (double) stats.getStats(championship).getDay() * p.getStats().getAverage()
                    * (1 + p.getStats().getGoals() * config.getEfficiencyCoefficient(PositionWrapper.fromStats(p.getPosition())));
            // round efficiency to 2 decimals
            p.setEfficiency(Math.round(efficiency * 100) / (double) 100);
        }

        // Fill MPG model
        for (Player player : players) {
            try {
                player.setEfficiency(stats.getStats(championship).getPlayer(player.getName()).getEfficiency());
            } catch (PlayerNotFoundException e) {
                if (failIfPlayerNotFound) {
                    throw e;
                }
                LOG.warn("WARN: Player can't be found in statistics: {}", player.getName());
                player.setEfficiency(0);
            }
        }
        return players;
    }

    private static CoachRequest getCoachRequest(Coach coach, List<Player> players, Config config) {
        int nbrDefenders = coach.getComposition() % 10;
        int nbrMidfielders = coach.getComposition() % 100 / 10;
        int nbrAttackers = coach.getComposition() / 100;

        CoachRequest request = new CoachRequest(coach);

        // Goals
        List<Player> goals = players.stream().filter(p -> p.getPosition().equals(Position.G)).collect(Collectors.toList());
        if (!goals.isEmpty()) {
            request.getPlayersOnPitch().setPlayer(1, goals.get(0).getId());
            if (goals.size() > 1) {
                request.getPlayersOnPitch().setPlayer(18, goals.get(1).getId());
            }
        }

        List<Player> defenders = players.stream().filter(p -> p.getPosition().equals(Position.D)).collect(Collectors.toList());
        List<Player> midfielders = players.stream().filter(p -> p.getPosition().equals(Position.M)).collect(Collectors.toList());
        List<Player> attackers = players.stream().filter(p -> p.getPosition().equals(Position.A)).collect(Collectors.toList());

        // Main lines
        setPlayersOnPitch(request, defenders, nbrDefenders, 1);
        setPlayersOnPitch(request, midfielders, nbrMidfielders, 1 + nbrDefenders);
        setPlayersOnPitch(request, attackers, nbrAttackers, 1 + nbrDefenders + nbrMidfielders);

        // Substitutes
        setPlayersOnPitch(request, defenders, 2, 11);
        setPlayersOnPitch(request, midfielders, 2, 13);
        setPlayersOnPitch(request, attackers, 2, 15);

        // Tactical Substitutes (x5)
        setTacticalSubstitute(request, 12, 1 + nbrDefenders, config.getNoteTacticalSubstituteDefender());
        setTacticalSubstitute(request, 14, 1 + nbrDefenders + nbrMidfielders, config.getNoteTacticalSubstituteMidfielder());
        setTacticalSubstitute(request, 15, nbrDefenders + nbrMidfielders, config.getNoteTacticalSubstituteMidfielder());
        setTacticalSubstitute(request, 16, 1 + nbrDefenders + nbrMidfielders + nbrAttackers, config.getNoteTacticalSubstituteAttacker());
        setTacticalSubstitute(request, 17, nbrDefenders + nbrMidfielders + nbrAttackers, config.getNoteTacticalSubstituteAttacker());

        return request;
    }

    private static void setPlayersOnPitch(CoachRequest request, List<Player> players, int number, int index) {
        for (int i = 0; i < number; i++) {
            if (!players.isEmpty()) {
                request.getPlayersOnPitch().setPlayer(index + i + 1, players.remove(0).getId());
            }
        }
    }

    private static void setTacticalSubstitute(CoachRequest request, int playerIdSubstitutePosition, int playerIdStartPosition, float rating) {
        String playerIdSubstitute = request.getPlayersOnPitch().getPlayer(playerIdSubstitutePosition);
        String playerIdStart = request.getPlayersOnPitch().getPlayer(playerIdStartPosition);
        if (StringUtils.isBlank(playerIdSubstitute) || StringUtils.isBlank(playerIdStart)) {
            return;
        }
        request.getTacticalsubstitutes().add(new TacticalSubstitute(playerIdSubstitute, playerIdStart, rating));
    }

    private static AsciiTable getTable(Object... columnTitle) {
        AsciiTable at = new AsciiTable();
        at.getContext().setGrid(A7_Grids.minusBarPlusEquals());
        at.setPaddingLeftRight(1);
        at.getRenderer().setCWC(new CWC_LongestLine());
        at.addRule();
        AT_Row rowHead = at.addRow(columnTitle);
        for (AT_Cell cell : rowHead.getCells()) {
            cell.getContext().setTextAlignment(TextAlignment.CENTER);
        }
        at.addRule();
        return at;
    }

    private static void setTableFormatRowPaddingSpace(AT_Row row) {
        for (AT_Cell cell : row.getCells()) {
            cell.getContext().setPaddingLeftRight(1);
        }
    }

}
