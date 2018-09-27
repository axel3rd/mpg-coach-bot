package org.blondin.mpg;

import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.Player;
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

        LOG.info("TODO: This is just a try for the moment ... display your players, stats available for {} players",
                mpgStatsClient.getStats().getPlayers().size());

        for (League league : mpgClient.getDashboard().getLeagues()) {
            LOG.info("========== {} ==========", league.getName());
            for (Player player : mpgClient.getCoach(league.getId()).getPlayers()) {
                LOG.info("{}", player.getName());
            }
        }
    }

}
