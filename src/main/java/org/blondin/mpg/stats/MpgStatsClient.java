package org.blondin.mpg.stats;

import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.stats.model.Championship;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgStatsClient extends AbstractClient {

    private static Championship cache;

    private MpgStatsClient() {
        super();
    }

    public static MpgStatsClient build() {
        return new MpgStatsClient();
    }

    @Override
    protected String getUrl() {
        return "https://www.mpgstats.fr/json/customteam.json";
    }

    public synchronized Championship getStats() {
        if (cache == null) {
            // TODO : Stats are updated every week (analyse JS for detail) => could be cached in file
            cache = get("Ligue-1", Championship.class);
        }
        return cache;
    }
}
