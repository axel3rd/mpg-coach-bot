package org.blondin.mpg;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.ChampionshipOutType;
import org.blondin.mpg.out.InjuredSuspendedWrapperClient;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.root.exception.NoMoreGamesException;
import org.blondin.mpg.root.exception.PlayerNotFoundException;
import org.blondin.mpg.root.model.ChampionshipType;
import org.blondin.mpg.root.model.Club;
import org.blondin.mpg.root.model.Clubs;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.CoachRequest;
import org.blondin.mpg.root.model.Division;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.LeagueStatus;
import org.blondin.mpg.root.model.Mode;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.root.model.PlayerStatus;
import org.blondin.mpg.root.model.PoolPlayers;
import org.blondin.mpg.root.model.Position;
import org.blondin.mpg.root.model.SelectedBonus;
import org.blondin.mpg.root.model.TacticalSubstitute;
import org.blondin.mpg.root.model.Team;
import org.blondin.mpg.stats.ChampionshipStatsType;
import org.blondin.mpg.stats.MpgStatsClient;
import org.blondin.mpg.stats.model.CurrentDay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vandermeer.asciitable.AT_Cell;
import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.asciithemes.a7.A7_Grids;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

public class Main {

    private static final String TABLE_POSITION = "P";
    private static final String TABLE_PLAYER_NAME = "Player name";
    private static final String TABLE_QUOTE = "Q.";
    private static final String TABLE_EFFICIENCY = "Eff.";

    private static final DecimalFormat FORMAT_DECIMAL_DOUBLE = new DecimalFormat("0.00");

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) { // NOSONAR : args used as file option, no security issue
        String configFile = null;
        if (args != null && args.length > 0) {
            configFile = args[0];
        }
        Config config = Config.build(configFile);
        MpgClient mpgClient = MpgClient.build(config);
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(config);
        InjuredSuspendedWrapperClient outPlayersClient = InjuredSuspendedWrapperClient.build(config);
        ApiClients apiClients = ApiClients.build(mpgClient, mpgStatsClient, outPlayersClient);
        process(apiClients, config);
    }

    static void process(ApiClients apiClients, Config config) {
        for (League league : apiClients.getMpg().getDashboard().getLeagues()) {
            if (LeagueStatus.TERMINATED.equals(league.getStatus())
                    || (!config.getLeaguesInclude().isEmpty() && !config.getLeaguesInclude().contains(league.getId()))
                    || (!config.getLeaguesExclude().isEmpty() && config.getLeaguesExclude().contains(league.getId()))) {
                // Don't display any logs
                continue;
            }
            processLeague(league, apiClients, config);
        }
    }

    static void processLeague(League league, ApiClients apiClients, Config config) {
        LOG.info("========== {} ==========", league.getName());
        if (league.getChampionship().equals(ChampionshipType.CHAMPIONS_LEAGUE)) {
            LOG.info("\nSorry, Champions League is currently not supported.\n");
            return;
        }
        switch (league.getStatus()) {
        case TERMINATED:
            // Already managed previously
        case KEEP:
            LOG.info("\nSome users should select players to kept before Mercato can start, come back soon !\n");
            break;
        case CREATION:
        case UNKNOWN:
            processMercatoChampionship(league, apiClients, config);
            break;
        case MERCATO:
            if (league.getCurrentTeamStatus() == 2) {
                LOG.info("\nMercato round is closed, come back soon for the next !\n");
                return;
            }
            if (league.getCurrentTeamStatus() == 3) {
                LOG.info("\nMercato will be ending, ready for your first match ?\n");
                return;
            }
            processMercatoLeague(league, apiClients, config);
            break;
        case GAMES:
            processGames(league, apiClients, config);
            break;
        }
    }

    static void processMercatoLeague(League league, ApiClients apiClients, Config config) {
        LOG.info("\nProposal for your mercato:\n");
        List<Player> players = apiClients.getMpg().getAvailablePlayers(league.getDivisionId()).getAvailablePlayers();
        completePlayersClub(players, apiClients.getMpg().getClubs());
        calculateEfficiency(players, apiClients.getStats(), ChampionshipTypeWrapper.toStats(league.getChampionship()), config, false, true);
        processMercato(players, apiClients.getOutPlayers(), ChampionshipTypeWrapper.toOut(league.getChampionship()));
    }

    static void processMercatoChampionship(League league, ApiClients apiClients, Config config) {
        LOG.info("\nProposal for your coming soon mercato:\n");
        List<Player> players = apiClients.getMpg().getPoolPlayers(league.getChampionship()).getPlayers();
        completePlayersClub(players, apiClients.getMpg().getClubs());
        calculateEfficiency(players, apiClients.getStats(), ChampionshipTypeWrapper.toStats(league.getChampionship()), config, false, true);
        processMercato(players, apiClients.getOutPlayers(), ChampionshipTypeWrapper.toOut(league.getChampionship()));
    }

    static void processMercato(List<Player> players, InjuredSuspendedWrapperClient outPlayersClient, ChampionshipOutType championship) {
        Collections.sort(players,
                Comparator.comparing(Player::getPosition).thenComparing(Player::getEfficiency).thenComparing(Player::getQuotation).reversed());
        List<Player> goals = players.stream().filter(p -> p.getPosition().equals(Position.G)).collect(Collectors.toList()).subList(0, 5);
        List<Player> defenders = players.stream().filter(p -> p.getPosition().equals(Position.D)).collect(Collectors.toList()).subList(0, 10);
        List<Player> midfielders = players.stream().filter(p -> p.getPosition().equals(Position.M)).collect(Collectors.toList()).subList(0, 10);
        List<Player> attackers = players.stream().filter(p -> p.getPosition().equals(Position.A)).collect(Collectors.toList()).subList(0, 10);

        AsciiTable at = getTable(TABLE_POSITION, TABLE_PLAYER_NAME, TABLE_EFFICIENCY, TABLE_QUOTE, "Out info");
        for (List<Player> line : Arrays.asList(goals, defenders, midfielders, attackers)) {
            for (Player player : line) {
                org.blondin.mpg.out.model.Player outPlayer = outPlayersClient.getPlayer(championship, player.getName(),
                        PositionWrapper.toOut(player.getPosition()), player.getClubName(), OutType.INJURY_GREEN);
                String outInfos = "";
                if (outPlayer != null) {
                    outInfos = String.format("%s - %s - %s", outPlayer.getOutType(), outPlayer.getDescription(), outPlayer.getLength());
                }
                AT_Row row = at.addRow(player.getPosition(), player.getName(), FORMAT_DECIMAL_DOUBLE.format(player.getEfficiency()),
                        player.getQuotation(), outInfos);
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

    static void processGames(League league, ApiClients apiClients, Config config) {
        try {

            // Get main bean for current league
            Division division = apiClients.getMpg().getDivision(league.getDivisionId());
            Team team = apiClients.getMpg().getTeam(division.getTeam(apiClients.getMpg().getUserId()));
            PoolPlayers pool = apiClients.getMpg().getPoolPlayers(league.getChampionship());
            Coach coach = apiClients.getMpg().getCoach(league.getDivisionId());
            completePlayersClub(pool.getPlayers(), apiClients.getMpg().getClubs());
            completePlayersTeam(team.getSquad(), pool);
            List<Player> players = team.getSquad().values().stream().collect(Collectors.toList());

            // Calculate efficiency (notes should be in injured players display), and save for transactions proposal
            calculateEfficiency(players, apiClients.getStats(), ChampionshipTypeWrapper.toStats(league.getChampionship()), config, false, true);
            List<Player> playersTeam = players.stream().collect(Collectors.toList());

            // Remove out players (and write them)
            removeOutPlayers(players, apiClients.getOutPlayers(), ChampionshipTypeWrapper.toOut(league.getChampionship()), true);

            // Sort by efficiency
            Collections.sort(players,
                    Comparator.comparing(Player::getPosition).thenComparing(Player::getEfficiency).thenComparing(Player::getQuotation).reversed());

            // Write optimized team
            writeTeamOptimized(players, config.isDebug());

            // Auto-update team
            if (config.isTeampUpdate()) {
                updateTeamWithRetry(apiClients.getMpg(), division, team, coach, players, config);
            }

            if (config.isTransactionsProposal()) {
                if (Mode.NORMAL.equals(league.getMode())) {
                    LOG.info(
                            "\nTransaction proposals can not be achieved, you should buy 'MPG expert mode' for this league (very fun, not expensive!)");
                } else {
                    LOG.info("\nTransactions proposal ...");
                    CurrentDay cd = apiClients.getStats().getStats(ChampionshipTypeWrapper.toStats(league.getChampionship())).getInfos()
                            .getAnnualStats().getCurrentDay();
                    if (cd.getLastDayReached() < cd.getDay()) {
                        LOG.info("\nWARNING: Last day stats have not fully reached! Please retry tomorrow");
                    }
                    List<Player> playersAvailable = apiClients.getMpg().getAvailablePlayers(league.getDivisionId()).getAvailablePlayers();
                    completePlayersClub(playersAvailable, apiClients.getMpg().getClubs());
                    removeOutPlayers(playersAvailable, apiClients.getOutPlayers(), ChampionshipTypeWrapper.toOut(league.getChampionship()), false);
                    calculateEfficiency(playersAvailable, apiClients.getStats(), ChampionshipTypeWrapper.toStats(league.getChampionship()), config,
                            false, false);
                    Integer currentPlayersBuy = team.getSquad().values().stream().filter(p -> p.getStatus().equals(PlayerStatus.PROPOSAL))
                            .map(Player::getPricePaid).collect(Collectors.summingInt(Integer::intValue));
                    writeTransactionsProposal(playersTeam, playersAvailable, team.getBudget() - currentPlayersBuy, apiClients.getOutPlayers(),
                            ChampionshipTypeWrapper.toOut(league.getChampionship()), config);
                }
            }
        } catch (NoMoreGamesException e) {
            LOG.info("\nNo more games in this league ...\n");
        }
        LOG.info("");
    }

    /**
     * Add club name for all pool players
     * 
     * @param players Pool
     * @param clubs Clubs
     */
    static void completePlayersClub(List<Player> players, Clubs clubs) {
        for (Player player : players) {
            Club club = clubs.getChampionshipClubs().get(player.getClubId());
            if (club == null) {
                throw new UnsupportedOperationException(
                        String.format("Club '%s' cannot be found for player '%s'", player.getClubId(), player.getName()));
            }
            player.setClubName(club.getName());
        }
    }

    /**
     * Teams players is only id and price paid => replace by real player
     * 
     * @param teamPlayers teams
     * @param pool players
     */
    static void completePlayersTeam(Map<String, Player> teamPlayers, PoolPlayers pool) {
        for (Entry<String, Player> entry : teamPlayers.entrySet()) {
            Player player = pool.getPlayer(entry.getKey());
            player.setPricePaid(teamPlayers.get(entry.getKey()).getPricePaid());
            teamPlayers.put(entry.getKey(), player);
        }
    }

    private static void updateTeamWithRetry(MpgClient mpgClient, Division division, Team team, Coach coach, List<Player> players, Config config) {
        LOG.info("\nUpdating team ...");
        final long maxRetry = 10;
        for (int i = 1; i <= 10; i++) {
            try {
                mpgClient.updateCoach(coach.getIdMatch(), getCoachRequest(team, coach, players, division.getGameRemaining(), config));
                break;
            } catch (UnsupportedOperationException e) {
                if (i == maxRetry || !"Unsupported status code: 400 Bad Request / Content: {\"error\":\"badRequest\"}".equals(e.getMessage())) {
                    throw e;
                }
                try {
                    LOG.info("Retrying Team update...");
                    Thread.sleep(5000);
                } catch (InterruptedException e1) { // NOSONAR : Sleep wanted
                    throw new UnsupportedOperationException(e1);
                }
            }
        }
    }

    private static void writeTransactionsProposal(List<Player> playersTeam, List<Player> playersAvailable, int budget,
            InjuredSuspendedWrapperClient outPlayersClient, ChampionshipOutType championship, Config config) {

        // Players with bad efficiency
        List<Player> players2Sell = playersTeam.stream().filter(p -> p.getEfficiency() <= config.getEfficiencySell(p.getPosition()))
                .collect(Collectors.toList());

        // Remove goalkeeper(s) (if exist) and same team as the first
        List<Player> goalkeepers = playersTeam.stream().filter(p -> p.getPosition().equals(Position.G))
                .sorted(Comparator.comparing(Player::getEfficiency).reversed()).collect(Collectors.toList());
        final Player goalFirst = goalkeepers.isEmpty() ? new Player() : goalkeepers.get(0);
        if (!goalkeepers.isEmpty()) {
            players2Sell.removeIf(p -> p.getPosition().equals(Position.G) && p.getClubId().equals(goalkeepers.get(0).getClubId()));
        }

        int cash = budget;
        if (!config.isEfficiencyRecentFocus() && !players2Sell.isEmpty()) {
            LOG.info("Players to sell (initial cash: {}):", budget);
            AsciiTable at = getTable(TABLE_POSITION, TABLE_PLAYER_NAME, TABLE_EFFICIENCY, TABLE_QUOTE);
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

        Player defenderLast = playersTeam.stream().filter(p -> p.getPosition().equals(Position.D))
                .sorted(Comparator.comparing(Player::getEfficiency).thenComparing(Player::getQuotation)).collect(Collectors.toList()).get(0);
        Player midfielderLast = playersTeam.stream().filter(p -> p.getPosition().equals(Position.M))
                .sorted(Comparator.comparing(Player::getEfficiency).thenComparing(Player::getQuotation)).collect(Collectors.toList()).get(0);
        Player attackerLast = playersTeam.stream().filter(p -> p.getPosition().equals(Position.A))
                .sorted(Comparator.comparing(Player::getEfficiency).thenComparing(Player::getQuotation)).collect(Collectors.toList()).get(0);
        cash += defenderLast.getQuotation() + midfielderLast.getQuotation() + attackerLast.getQuotation();
        LOG.info("Budget if last field players by line sold: {}", cash);

        final int budgetPotential = cash;
        List<Player> players2buy = new ArrayList<>();
        players2buy.addAll(playersAvailable.stream().filter(p -> p.getPosition().equals(Position.G)).filter(p -> p.getQuotation() <= budgetPotential)
                .filter(p -> p.getEfficiency() > goalFirst.getEfficiency()).filter(p -> p.getEfficiency() > config.getEfficiencySell(Position.G))
                .sorted(Comparator.comparing(Player::getEfficiency).thenComparing(Player::getQuotation).reversed()).limit(3)
                .collect(Collectors.toList()));
        players2buy.addAll(playersAvailable.stream().filter(p -> p.getPosition().equals(Position.D)).filter(p -> p.getQuotation() <= budgetPotential)
                .filter(p -> p.getEfficiency() > defenderLast.getEfficiency()).filter(p -> p.getEfficiency() > config.getEfficiencySell(Position.D))
                .sorted(Comparator.comparing(Player::getEfficiency).thenComparing(Player::getQuotation).reversed()).limit(3)
                .collect(Collectors.toList()));
        players2buy.addAll(playersAvailable.stream().filter(p -> p.getPosition().equals(Position.M)).filter(p -> p.getQuotation() <= budgetPotential)
                .filter(p -> p.getEfficiency() > midfielderLast.getEfficiency()).filter(p -> p.getEfficiency() > config.getEfficiencySell(Position.M))
                .sorted(Comparator.comparing(Player::getEfficiency).thenComparing(Player::getQuotation).reversed()).limit(3)
                .collect(Collectors.toList()));
        players2buy.addAll(playersAvailable.stream().filter(p -> p.getPosition().equals(Position.A)).filter(p -> p.getQuotation() <= budgetPotential)
                .filter(p -> p.getEfficiency() > attackerLast.getEfficiency()).filter(p -> p.getEfficiency() > config.getEfficiencySell(Position.A))
                .sorted(Comparator.comparing(Player::getEfficiency).thenComparing(Player::getQuotation).reversed()).limit(3)
                .collect(Collectors.toList()));

        if (!players2buy.isEmpty()) {
            LOG.info("Player(s) to buy (3 best choice by line):");
            AsciiTable at = getTable(TABLE_POSITION, TABLE_PLAYER_NAME, TABLE_EFFICIENCY, TABLE_QUOTE);
            for (Player player : players2buy) {
                org.blondin.mpg.out.model.Player outPlayer = outPlayersClient.getPlayer(championship, player.getName(),
                        PositionWrapper.toOut(player.getPosition()), player.getClubName(), OutType.INJURY_GREEN);
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

    static List<Player> removeOutPlayers(List<Player> players, InjuredSuspendedWrapperClient outPlayersClient, ChampionshipOutType championship,
            boolean displayOut) {
        List<Player> outPlayers = new ArrayList<>();
        for (Player player : players) {
            org.blondin.mpg.out.model.Player outPlayer = outPlayersClient.getPlayer(championship, player.getName(),
                    PositionWrapper.toOut(player.getPosition()), player.getClubName(), OutType.INJURY_GREEN);
            if (outPlayer != null) {
                outPlayers.add(player);
                if (displayOut) {
                    String eff = FORMAT_DECIMAL_DOUBLE.format(player.getEfficiency());
                    LOG.info("Out: {} ({} - Eff.:{} / Q.:{} / Paid:{}) - {} - {} - {}", player.getName(), player.getPosition(), eff,
                            player.getQuotation(), player.getPricePaid(), outPlayer.getOutType(), outPlayer.getDescription(), outPlayer.getLength());
                }
            }
        }
        players.removeAll(outPlayers);
        return players;
    }

    private static void writeTeamOptimized(List<Player> players, boolean isDebug) {
        LOG.info("\nOptimized team:");
        AsciiTable at = getTable(TABLE_POSITION, TABLE_PLAYER_NAME, TABLE_EFFICIENCY, TABLE_QUOTE);
        Position lp = Position.G;
        for (Player player : players) {
            // Write position separator
            if (!player.getPosition().equals(lp)) {
                lp = player.getPosition();
                at.addRule();
            }
            String playerName = player.getName();
            if (isDebug) {
                playerName += " (" + player.getId() + ")";
            }
            AT_Row row = at.addRow(player.getPosition(), playerName, FORMAT_DECIMAL_DOUBLE.format(player.getEfficiency()), player.getQuotation());
            setTableFormatRowPaddingSpace(row);
            row.getCells().get(2).getContext().setTextAlignment(TextAlignment.RIGHT);
        }
        at.addRule();
        String render = at.render();
        LOG.info(render);
    }

    private static List<Player> calculateEfficiency(List<Player> players, MpgStatsClient stats, ChampionshipStatsType championship, Config config,
            boolean failIfPlayerNotFound, boolean logWarnIfPlayerNotFound) {
        int daysPeriod = getCurrentDay(stats, championship);
        int days4efficiency = 0;
        if (config.isEfficiencyRecentFocus()) {
            days4efficiency = config.getEfficiencyRecentDays();
            // If season start (=> daysPeriod < 8 when days4efficiency = 8 by default), the focus is on the started days
            if (daysPeriod < days4efficiency) {
                days4efficiency = daysPeriod;
            } else {
                daysPeriod = days4efficiency;
            }
        }
        for (org.blondin.mpg.stats.model.Player p : stats.getStats(championship).getPlayers()) {
            double efficiency = p.getStats().getMatchs(days4efficiency) / (double) daysPeriod * p.getStats().getAverage(days4efficiency)
                    * (1 + p.getStats().getGoals(days4efficiency) * config.getEfficiencyCoefficient(PositionWrapper.fromStats(p.getPosition())));
            // round efficiency to 2 decimals
            p.setEfficiency(efficiency);
        }

        // Fill MPG model
        for (Player player : players) {
            try {
                player.setEfficiency(stats.getStats(championship).getPlayer(player.getName()).getEfficiency());
            } catch (PlayerNotFoundException e) {
                if (failIfPlayerNotFound) {
                    throw e;
                }
                if (logWarnIfPlayerNotFound) {
                    LOG.warn("WARN: Player can't be found in statistics: {}", player.getName());
                }
                player.setEfficiency(0);
            }
        }
        return players;
    }

    private static int getCurrentDay(MpgStatsClient stats, ChampionshipStatsType championship) {
        int daysPeriod = stats.getStats(championship).getInfos().getAnnualStats().getCurrentDay().getDay();
        // If league not started, we take the number of day of season, because average will be on this period
        if (daysPeriod == 0 || stats.getStats(championship).getInfos().getAnnualStats().getCurrentDay().getPlayed() == 0) {
            // The previous season statistics could be null, in this case current annual max day is used
            daysPeriod = stats.getStats(championship).getInfos().getLastStats() == null
                    ? stats.getStats(championship).getInfos().getAnnualStats().getMaxDay()
                    : stats.getStats(championship).getInfos().getLastStats().getMaxDay();
        }
        return daysPeriod;
    }

    private static CoachRequest getCoachRequest(Team team, Coach coach, List<Player> players, int gameRemaining, Config config) {
        int nbrAttackers = coach.getComposition() % 10;
        int nbrMidfielders = coach.getComposition() % 100 / 10;
        int nbrDefenders = coach.getComposition() / 100;

        CoachRequest request = new CoachRequest(coach.getComposition());

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

        String playerIdForBonus = midfielders.get(0).getId();
        request.setBonusSelected(selectBonus(coach.getBonusSelected(), team.getBonuses(), gameRemaining, config.isUseBonus(), playerIdForBonus));

        // Main lines
        setPlayersOnPitch(request, defenders, nbrDefenders, 1);
        setPlayersOnPitch(request, midfielders, nbrMidfielders, 1 + nbrDefenders);
        setPlayersOnPitch(request, attackers, nbrAttackers, 1 + nbrDefenders + nbrMidfielders);

        // Substitutes
        int substitutes = 0;
        substitutes += setPlayersOnPitch(request, defenders, 2, 11);
        substitutes += setPlayersOnPitch(request, midfielders, 2, 13);
        substitutes += setPlayersOnPitch(request, attackers, 2, 15);

        // Tactical Substitutes (x5)
        if (config.isTacticalSubstitutes()) {
            setTacticalSubstitute(request, 12, 1 + nbrDefenders, config.getNoteTacticalSubstituteDefender());
            setTacticalSubstitute(request, 14, 1 + nbrDefenders + nbrMidfielders, config.getNoteTacticalSubstituteMidfielder());
            setTacticalSubstitute(request, 15, nbrDefenders + nbrMidfielders, config.getNoteTacticalSubstituteMidfielder());
            setTacticalSubstitute(request, 16, 1 + nbrDefenders + nbrMidfielders + nbrAttackers, config.getNoteTacticalSubstituteAttacker());
            setTacticalSubstitute(request, 17, nbrDefenders + nbrMidfielders + nbrAttackers, config.getNoteTacticalSubstituteAttacker());
        }

        // No blank on substitutes' bench
        if (substitutes < 6) {
            List<Player> playersRemaining = new ArrayList<>();
            playersRemaining.addAll(defenders);
            playersRemaining.addAll(midfielders);
            playersRemaining.addAll(attackers);
            Collections.sort(playersRemaining, Comparator.comparing(Player::getEfficiency).thenComparing(Player::getQuotation).reversed());
            for (int i = 12; i <= 17; i++) {
                if (playersRemaining.isEmpty()) {
                    break;
                }
                if (StringUtils.isBlank(request.getPlayersOnPitch().getPlayer(i))) {
                    request.getPlayersOnPitch().setPlayer(i, playersRemaining.remove(0).getId()); // NOSONAR : Pop first list item
                }
            }
        }

        // If Bonus is player power up (type=4 / RedBull/ UberEats), verify that player is on pitch, override otherwise
        verifyBonusPlayerOverrideOnPitch(request, playerIdForBonus);

        return request;
    }

    static void verifyBonusPlayerOverrideOnPitch(CoachRequest request, String playerIdEnfored) {
        if (request.getBonusSelected() != null && "boostOnePlayer".equals(request.getBonusSelected().getName())) {
            boolean onPitch = false;
            String playerIdSelected = request.getBonusSelected().getPlayerId();
            for (int i = 1; i <= 11; i++) {
                if (playerIdSelected.equals(request.getPlayersOnPitch().getPlayer(i))) {
                    onPitch = true;
                    break;
                }
            }
            if (!onPitch) {
                request.getBonusSelected().setPlayerId(playerIdEnfored);
            }
        }
    }

    static SelectedBonus selectBonus(SelectedBonus previousBonus, Map<String, Integer> bonuses, int matchsRemaining, boolean useBonus,
            String playerIdIfNeeded) {
        if (!useBonus || (previousBonus != null && previousBonus.getName() != null)) {
            return previousBonus;
        }
        if (bonuses == null) {
            throw new UnsupportedOperationException("Bonus is null, technical problem");
        }
        SelectedBonus bonusSelected = null;
        if (bonuses.values().stream().reduce(0, Integer::sum) >= matchsRemaining) {
            String bonus = getBestBonus(bonuses, matchsRemaining);
            bonusSelected = new SelectedBonus();
            bonusSelected.setName(bonus);
            if ("boostOnePlayer".equals(bonus)) {
                bonusSelected.setPlayerId(playerIdIfNeeded);
            }
        }
        return bonusSelected;
    }

    private static String getBestBonus(Map<String, Integer> bonuses, int matchsRemaining) {
        int bonusTooMuch = bonuses.values().stream().reduce(0, Integer::sum) - matchsRemaining - 1;
        List<String> bonusLowerPriority = SelectedBonus.getBonusPriority().stream().collect(Collectors.toList());
        Collections.reverse(bonusLowerPriority);
        for (String b : bonusLowerPriority) {
            for (int bi = 0; bi < bonuses.get(b); bi++) {
                if (bonusTooMuch > 0) {
                    bonusTooMuch--;
                } else {
                    return b;
                }
            }
        }
        throw new UnsupportedOperationException("Bonus cannot be null here, bug in selection algorithm !");
    }

    private static int setPlayersOnPitch(CoachRequest request, List<Player> players, int number, int index) {
        int setted = 0;
        for (int i = 0; i < number; i++) {
            if (!players.isEmpty()) {
                request.getPlayersOnPitch().setPlayer(index + i + 1, players.remove(0).getId()); // NOSONAR : Pop first list item
                setted++;
            }
        }
        return setted;
    }

    private static void setTacticalSubstitute(CoachRequest request, int playerIdSubstitutePosition, int playerIdStartPosition, float rating) {
        String playerIdSubstitute = request.getPlayersOnPitch().getPlayer(playerIdSubstitutePosition);
        String playerIdStart = request.getPlayersOnPitch().getPlayer(playerIdStartPosition);
        if (StringUtils.isBlank(playerIdSubstitute) || StringUtils.isBlank(playerIdStart)) {
            return;
        }
        request.getTacticalSubstitutes().add(new TacticalSubstitute(playerIdSubstitute, playerIdStart, rating));
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
