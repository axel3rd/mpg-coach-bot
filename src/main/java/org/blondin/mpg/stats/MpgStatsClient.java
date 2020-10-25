package org.blondin.mpg.stats;

import java.util.Date;
import java.util.EnumMap;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.stats.model.Championship;
import org.blondin.mpg.stats.model.LeaguesRefresh;

/**
 * Client for https://www.mpgstats.fr/ (Backend API is https://api.mlnstats.com/ since end of 2019)
 */
public class MpgStatsClient extends AbstractClient {

    private EnumMap<ChampionshipStatsType, Championship> cache = new EnumMap<>(ChampionshipStatsType.class);

    private MpgStatsClient(Config config) {
        super(config);
    }

    public static MpgStatsClient build(Config config) {
        return build(config, null);
    }

    public static MpgStatsClient build(Config config, String urlOverride) {
        MpgStatsClient client = new MpgStatsClient(config);
        client.setUrl(StringUtils.defaultString(urlOverride, "https://api.mlnstats.com"));
        return client;
    }

    public synchronized Championship getStats(ChampionshipStatsType type) {
        if (!cache.containsKey(type)) {
            // FR : "Ligue-1" / EN : "Premier-League" / ES : "Liga"
            // Call with infinite cache and verify timestamp after
            Championship championship = get("leagues/" + type.getValue(), Championship.class, 0);
            Date leagueDateRefresh = getLeaguesRefresh().getDate(championship.getInfos().getId());
            if (leagueDateRefresh == null || championship.getDate().before(leagueDateRefresh)) {
                // Force refresh by using a mini cache time
                championship = get("leagues/" + type.getValue(), Championship.class, 1);
            }
            cache.put(type, championship);
        }
        return cache.get(type);
    }

    protected synchronized LeaguesRefresh getLeaguesRefresh() {
        return get("builds", LeaguesRefresh.class, TIME_HOUR_IN_MILLI_SECOND);
    }
}
