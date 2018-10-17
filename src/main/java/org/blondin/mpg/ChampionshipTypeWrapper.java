package org.blondin.mpg;

import org.blondin.mpg.equipeactu.ChampionshipOutType;
import org.blondin.mpg.root.model.ChampionshipType;
import org.blondin.mpg.stats.ChampionshipStatsType;

public class ChampionshipTypeWrapper {

    ChampionshipTypeWrapper() {
        super();
    }

    public static ChampionshipStatsType toStats(ChampionshipType championship) {
        switch (championship) {
        case LIGUE_1:
            return ChampionshipStatsType.LIGUE_1;
        case PREMIER_LEAGUE:
            return ChampionshipStatsType.PREMIER_LEAGUE;
        case LIGA:
            return ChampionshipStatsType.LIGA;
        default:
            throw new UnsupportedOperationException(String.format("Championship type not supported: %s", championship));
        }
    }

    public static ChampionshipOutType toOut(ChampionshipType championship) {
        switch (championship) {
        case LIGUE_1:
            return ChampionshipOutType.LIGUE_1;
        case PREMIER_LEAGUE:
            return ChampionshipOutType.PREMIER_LEAGUE;
        case LIGA:
            return ChampionshipOutType.LIGA;
        default:
            throw new UnsupportedOperationException(String.format("Championship type not supported: %s", championship));
        }
    }
}
