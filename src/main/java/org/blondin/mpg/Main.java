package org.blondin.mpg;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.InjuredSuspendedWrapperClient;
import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.root.model.ChampionshipType;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.LeagueStatus;
import org.blondin.mpg.stats.MpgStatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) { // NOSONAR : args used as file option, no security issue
        String configFile = null;
        if (args != null && args.length > 0) {
            configFile = args[0];
        }
        Config config = Config.build(configFile);
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(config);
        InjuredSuspendedWrapperClient outPlayersClient = InjuredSuspendedWrapperClient.build(config);
        MpgClient mpgClient = MpgClient.build(config);
        ApiClients apiClients = ApiClients.build(mpgClient, mpgStatsClient, outPlayersClient);
        process(apiClients, config);
    }

    static void process(ApiClients apiClients, Config config) {
        for (League league : apiClients.getMpg().getDashboard().getLeagues()) {
            if (league.isFollowed() || LeagueStatus.TERMINATED.equals(league.getStatus()) || (!config.getLeaguesInclude().isEmpty() && !config.getLeaguesInclude().contains(league.getId()))
                    || (!config.getLeaguesExclude().isEmpty() && config.getLeaguesExclude().contains(league.getId()))) {
                // Don't display any logs
                continue;
            }
            processLeague(league, apiClients, config);
        }
    }

    static void processLeague(League league, ApiClients apiClients, Config config) {
        LOG.info("========== {} ({}) ==========", league.getName(), league.getId());
        if (league.getChampionship().equals(ChampionshipType.CHAMPIONS_LEAGUE)) {
            LOG.info("\nSorry, Champions League is currently not supported.\n");
            return;
        }
        if (league.getChampionship().equals(ChampionshipType.LIGUE_SUPER)) {
            LOG.info("\nSorry, Ligue Super is currently not supported.\n");
            return;
        }
        switch (league.getStatus()) {
        case TERMINATED, KEEP:
            // TERMINATED already managed previously
            LOG.info("\nSome users should select players to kept before Mercato can start, come back soon !\n");
            break;
        case CREATION, UNKNOWN:
            new MercatoChampionship().process(league, apiClients, config);
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
            new MercatoLeague().process(league, apiClients, config);
            break;
        case GAMES:
            if (league.isLive() && StringUtils.isBlank(league.getNextRealGameWeekDate())) {
                LOG.info("\nThis is the last live day, no next week.\n");
                return;
            }
            new Games().process(league, apiClients, config);
            break;
        }
    }

}
