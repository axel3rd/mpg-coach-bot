package org.blondin.mpg.stats;

import java.util.EnumMap;

import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.stats.model.Championship;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgStatsClient extends AbstractClient {

    private static EnumMap<ChampionshipStatsType, Championship> cache = new EnumMap<>(ChampionshipStatsType.class);

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

    public synchronized Championship getStats(ChampionshipStatsType type) {
        if (!cache.containsKey(type)) {
            // TODO : Stats are updated every week (analyse JS for detail) => could be cached in file

            // FR : "Ligue-1"
            // EN : "Premier-League"
            // ES : "Liga"
            cache.put(type, get(type.getValue(), Championship.class));
        }
        return cache.get(type);
    }
}
