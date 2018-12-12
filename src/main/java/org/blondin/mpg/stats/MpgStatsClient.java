package org.blondin.mpg.stats;

import java.util.EnumMap;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.stats.model.Championship;
import org.blondin.mpg.stats.model.LeaguesRefresh;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgStatsClient extends AbstractClient {

    private EnumMap<ChampionshipStatsType, Championship> cache = new EnumMap<>(ChampionshipStatsType.class);

    private MpgStatsClient() {
        super();
    }

    public static MpgStatsClient build(Config config) {
        return build(config, null);
    }

    public static MpgStatsClient build(Config config, String urlOverride) {
        MpgStatsClient client = new MpgStatsClient();
        client.setUrl(StringUtils.defaultString(urlOverride, "https://www.mpgstats.fr/json"));
        client.setProxy(config.getProxy());
        return client;
    }

    public synchronized Championship getStats(ChampionshipStatsType type) {
        if (!cache.containsKey(type)) {
            // FR : "Ligue-1" / EN : "Premier-League" / ES : "Liga"
            // Call with infinite cache and verify timestamp after
            LeaguesRefresh leaguesRefresh = getLeaguesRefresh();
            Championship championship = get("customteam.json/" + type.getValue(), Championship.class, 0);
            if (championship.getDate().before(leaguesRefresh.getDate(championship.getInfos().getId()))) {
                // Force refresh by using a mini cache time
                championship = get("customteam.json/" + type.getValue(), Championship.class, 1);
            }
            cache.put(type, championship);
        }
        return cache.get(type);
    }

    protected synchronized LeaguesRefresh getLeaguesRefresh() {
        return get("leagues.json", LeaguesRefresh.class, TIME_HOUR_IN_MILLI_SECOND);
    }
}
