package org.blondin.mpg;

import java.text.DecimalFormat;
import java.util.List;

import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.exception.PlayerNotFoundException;
import org.blondin.mpg.root.model.Club;
import org.blondin.mpg.root.model.Clubs;
import org.blondin.mpg.root.model.Player;
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

public abstract class AbstractMpgProcess implements MpgProcess {

    protected static final String TABLE_POSITION = "P";
    protected static final String TABLE_PLAYER_NAME = "Player name";
    protected static final String TABLE_QUOTE = "Q.";
    protected static final String TABLE_EFFICIENCY = "Eff.";
    protected static final DecimalFormat FORMAT_DECIMAL_DOUBLE = new DecimalFormat("0.00");

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMpgProcess.class);

    protected AbstractMpgProcess() {
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

    protected static List<Player> completeAuctionAndcalculateEfficiency(List<Player> players, MpgStatsClient stats,
            ChampionshipStatsType championship, Config config, boolean failIfPlayerNotFound, boolean logWarnIfPlayerNotFound) {

        // Pre-process efficiencies
        calculateEfficiencies(stats, championship, config);

        // Fill MPG model
        for (Player player : players) {
            try {
                org.blondin.mpg.stats.model.Player p = stats.getStats(championship).getPlayer(player.getName());
                if (p.getAuction() != null && p.getAuction().getNumber() > 5) {
                    // Feature in API only since 2021-11
                    player.setAuction(p.getAuction().getAverage());
                }
                if (player.getAuction() == 0 && p.getAuctionLong() != null) {
                    // Feature in API only since 2022-07
                    player.setAuction(p.getAuctionLong().getAverage());
                }
                player.setEfficiency(p.getEfficiency());
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

    private static void calculateEfficiencies(MpgStatsClient stats, ChampionshipStatsType championship, Config config) {
        int daysPeriod = getCurrentDay(stats, championship);
        int days4efficiency = 0;
        if (config.isEfficiencyRecentFocus() && stats.getStats(championship).getInfos().getAnnualStats().getCurrentDay().getDayReached() > 0) {
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
    }

    private static int getCurrentDay(MpgStatsClient stats, ChampionshipStatsType championship) {
        int daysPeriod = stats.getStats(championship).getInfos().getAnnualStats().getCurrentDay().getDayReached();
        // If league not started, we take the number of day of season, because average will be on this period
        if (daysPeriod == 0) {
            // The previous season statistics could be null, in this case current annual max day is used
            daysPeriod = stats.getStats(championship).getInfos().getLastStats() == null
                    ? stats.getStats(championship).getInfos().getAnnualStats().getMaxDay()
                    : stats.getStats(championship).getInfos().getLastStats().getMaxDay();
        }
        return daysPeriod;
    }

    protected static AsciiTable getTable(Object... columnTitle) {
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

    protected static void setTableFormatRowPaddingSpace(AT_Row row) {
        for (AT_Cell cell : row.getCells()) {
            cell.getContext().setPaddingLeftRight(1);
        }
    }
}
