package org.blondin.mpg;

import java.util.ArrayList;
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
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.CoachRequest;
import org.blondin.mpg.root.model.Division;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.Mode;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.root.model.PlayersOnPitch;
import org.blondin.mpg.root.model.PoolPlayers;
import org.blondin.mpg.root.model.Position;
import org.blondin.mpg.root.model.SelectedBonus;
import org.blondin.mpg.root.model.TacticalSubstitute;
import org.blondin.mpg.root.model.Team;
import org.blondin.mpg.stats.model.CurrentDay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

public class Games extends AbstractMpgProcess {

    private static final Logger LOG = LoggerFactory.getLogger(Games.class);

    public void process(League league, ApiClients apiClients, Config config) {
        try {

            // Get main bean for current league
            Division division = apiClients.getMpg().getDivision(league.getDivisionId());
            Team team = apiClients.getMpg().getTeam(division.getTeam(apiClients.getMpg().getUserId()));
            PoolPlayers pool = apiClients.getMpg().getPoolPlayers(league.getChampionship());
            Coach coach = apiClients.getMpg().getCoach(league.getDivisionId());
            completePlayersClub(pool.getPlayers(), apiClients.getMpg().getClubs());
            completePlayersTeam(team.getSquad(), pool);
            List<Player> players = team.getSquad().values().stream().collect(Collectors.toList());

            // Complete auction and calculate efficiency (notes should be in injured players display), and save for transactions proposal
            completeAuctionAndcalculateEfficiency(players, apiClients.getStats(), ChampionshipTypeWrapper.toStats(league.getChampionship()), config,
                    false, true);
            List<Player> playersTeam = players.stream().collect(Collectors.toList());

            // Remove out players (and write them)
            removeOutPlayers(players, apiClients.getOutPlayers(), ChampionshipTypeWrapper.toOut(league.getChampionship()), true);

            // Sort by efficiency
            Collections.sort(players,
                    Comparator.comparing(Player::getPosition).thenComparing(Player::getEfficiency).thenComparing(Player::getQuotation).reversed());

            // Write optimized team
            writeTeamOptimized(players, coach.getComposition(), config.isDebug());

            // Auto-update team
            if (config.isTeampUpdate()) {
                updateTeamWithRetry(apiClients.getMpg(), division, team, coach, players, pool, config);
            }

            if (config.isTransactionsProposal()) {
                if (Mode.NORMAL.equals(league.getMode())) {
                    LOG.info(
                            "\nTransaction proposals can not be achieved, you should buy 'MPG expert mode' for this league (very fun, not expensive!)");
                } else {
                    LOG.info("\nTransactions proposal ...");
                    CurrentDay cd = apiClients.getStats().getStats(ChampionshipTypeWrapper.toStats(league.getChampionship())).getInfos()
                            .getAnnualStats().getCurrentDay();
                    if (!cd.isStatsDayReached()) {
                        LOG.info("\nWARNING: Last day stats have not fully reached! Please retry tomorrow");
                    }
                    List<Player> playersAvailable = apiClients.getMpg().getAvailablePlayers(league.getDivisionId()).getList();
                    completePlayersClub(playersAvailable, apiClients.getMpg().getClubs());
                    removeOutPlayers(playersAvailable, apiClients.getOutPlayers(), ChampionshipTypeWrapper.toOut(league.getChampionship()), false);
                    completeAuctionAndcalculateEfficiency(playersAvailable, apiClients.getStats(),
                            ChampionshipTypeWrapper.toStats(league.getChampionship()), config, false, false);

                    Integer currentPlayersBuy = team.getBids().stream().map(Player::getPricePaid).collect(Collectors.summingInt(Integer::intValue));
                    writeTransactionsProposal(cd.getDay(), playersTeam, playersAvailable, team.getBudget() - currentPlayersBuy,
                            apiClients.getOutPlayers(), ChampionshipTypeWrapper.toOut(league.getChampionship()), config);
                }
            }
        } catch (NoMoreGamesException e) {
            LOG.info("\nNo more games in this league ...\n");
        }
        LOG.info("");
    }

    /**
     * Teams players is only id and price paid => replace by real player
     * 
     * @param teamPlayers teams
     * @param pool        players
     */
    static void completePlayersTeam(Map<String, Player> teamPlayers, PoolPlayers pool) {
        List<String> players2Remove = new ArrayList<>();
        for (Entry<String, Player> entry : teamPlayers.entrySet()) {
            try {
                Player player = pool.getPlayer(entry.getKey());
                player.setPricePaid(teamPlayers.get(entry.getKey()).getPricePaid());
                teamPlayers.put(entry.getKey(), player);
            } catch (PlayerNotFoundException e) {
                LOG.warn("Some player in your team removed because doesn't exist in league pool players: {}", entry.getKey());
                players2Remove.add(entry.getKey());
            }
        }
        for (String p2r : players2Remove) {
            teamPlayers.remove(p2r);
        }
    }

    private static void updateTeamWithRetry(MpgClient mpgClient, Division division, Team team, Coach coach, List<Player> players, PoolPlayers pool,
            Config config) {
        LOG.info("\nUpdating team ...");
        CoachRequest request = getCoachRequest(team, coach, players, division.getGameRemaining(), config);
        if (StringUtils.isNotBlank(request.getCaptain())) {
            LOG.info("  Captain: {}", pool.getPlayer(request.getCaptain()).getName());
        }
        if (request.getBonusSelected() != null && StringUtils.isNotBlank(request.getBonusSelected().getName())) {
            String playerPotential = "";
            if (SelectedBonus.BONUS_BOOT_ONE_PLAYER.equals(request.getBonusSelected().getName())) {
                playerPotential = "(" + pool.getPlayer(request.getBonusSelected().getPlayerId()).getName() + ")";
            }
            LOG.info("  Bonus  : {} {}", request.getBonusSelected().getName(), playerPotential);
        }
        final long maxRetry = 10;
        for (int i = 1; i <= 10; i++) {
            try {
                mpgClient.updateCoach(coach.getIdMatch(), request);
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

    private static void writeTransactionsProposal(int currentDay, List<Player> playersTeam, List<Player> playersAvailable, int budget,
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
        if (currentDay > 2 && !players2Sell.isEmpty()) {
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

        // Check if some goalkeeper(s) always on pitch
        List<Player> goals = players.stream().filter(p -> Position.G.equals(p.getPosition())).collect(Collectors.toList());
        if (goals.isEmpty()) {
            LOG.warn("\nWARNING: All goalkeeper(s) are injured/absent, so maintained on the pitch!");
            players.addAll(outPlayers.stream().filter(p -> Position.G.equals(p.getPosition())).collect(Collectors.toList()));
        }

        return players;
    }

    private static void writeTeamOptimized(List<Player> players, int composition, boolean isDebug) {
        LOG.info("\nOptimized team (Compo: {}):", composition);
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
        String playerIdForCaptain = request.getBonusSelected() != null
                && SelectedBonus.BONUS_BOOT_ONE_PLAYER.equals(request.getBonusSelected().getName())
                && !midfielders.get(1).getId().equals(request.getBonusSelected().getPlayerId()) ? midfielders.get(1).getId() : playerIdForBonus;
        request.setCaptain(selectCapatain(coach.getCaptain(), playerIdForCaptain, playerIdForBonus, config.isUseBonus()));

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

        // If Bonus is player power up (boostOnePlayer), verify that player is on pitch, override otherwise
        verifyBonusPlayersOverrideOnPitch(request, playerIdForBonus, playerIdForCaptain);

        return request;
    }

    static void verifyBonusPlayersOverrideOnPitch(CoachRequest request, String playerIdForBonusEnforced, String playerIdForCaptainEnforced) {
        // Bonus
        if (request.getBonusSelected() != null && SelectedBonus.BONUS_BOOT_ONE_PLAYER.equals(request.getBonusSelected().getName())
                && !verifyPlayerOnPitch(request.getPlayersOnPitch(), request.getBonusSelected().getPlayerId())) {
            request.getBonusSelected().setPlayerId(playerIdForBonusEnforced);
        }

        // Captain
        if (StringUtils.isNotBlank(request.getCaptain()) && !verifyPlayerOnPitch(request.getPlayersOnPitch(), request.getCaptain())) {
            request.setCaptain(playerIdForCaptainEnforced);
        }
    }

    static boolean verifyPlayerOnPitch(PlayersOnPitch playersOnPitch, String playerId) {
        if (StringUtils.isBlank(playerId)) {
            return false;
        }
        for (int i = 1; i <= 11; i++) {
            if (playerId.equals(playersOnPitch.getPlayer(i))) {
                return true;
            }
        }
        return false;
    }

    static String selectCapatain(String previousCaptain, String captainIdIfNeeded, String currentPlayerBonus, boolean useBonus) {
        if (!useBonus || (StringUtils.isNotBlank(previousCaptain) && !previousCaptain.equals(currentPlayerBonus))) {
            return previousCaptain;
        }
        return captainIdIfNeeded;
    }

    static SelectedBonus selectBonus(SelectedBonus previousBonus, Map<String, Integer> bonuses, int matchsRemaining, boolean useBonus,
            String playerIdIfNeeded) {
        if (!useBonus || (previousBonus != null && previousBonus.getName() != null)) {
            return previousBonus;
        }
        if (bonuses == null) {
            throw new UnsupportedOperationException("Bonus is null, technical problem");
        }

        // Remove decat bonus, not supported (https://github.com/axel3rd/mpg-coach-bot/issues/234)
        bonuses.remove("fourStrikers");

        SelectedBonus bonusSelected = null;
        if (bonuses.values().stream().reduce(0, Integer::sum) >= matchsRemaining) {
            String bonus = getBestBonus(bonuses, matchsRemaining);
            bonusSelected = new SelectedBonus();
            bonusSelected.setName(bonus);
            if (SelectedBonus.BONUS_BOOT_ONE_PLAYER.equals(bonus)) {
                bonusSelected.setPlayerId(playerIdIfNeeded);
            }
        }
        return bonusSelected;
    }

    private static String getBestBonus(Map<String, Integer> bonuses, int matchsRemaining) {
        if (matchsRemaining == 0) {
            throw new UnsupportedOperationException("0 match remaining, using this method is not logic, bug in algorithm !");
        }
        int bonusTooMuch = bonuses.values().stream().reduce(0, Integer::sum) - matchsRemaining;
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
}
