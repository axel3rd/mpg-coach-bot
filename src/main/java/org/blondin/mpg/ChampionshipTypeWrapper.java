package org.blondin.mpg;

import org.blondin.mpg.out.ChampionshipOutType;
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
        case LIGUE_2:
            return ChampionshipStatsType.LIGUE_2;
        case PREMIER_LEAGUE:
            return ChampionshipStatsType.PREMIER_LEAGUE;
        case LIGA:
            return ChampionshipStatsType.LIGA;
        case SERIE_A:
            return ChampionshipStatsType.SERIE_A;
        default:
            throw new UnsupportedOperationException(String.format("Championship type not supported: %s", championship));
        }
    }

    public static ChampionshipOutType toOut(ChampionshipType championship) {
        switch (championship) {
        case LIGUE_1:
            return ChampionshipOutType.LIGUE_1;
        case LIGUE_2:
            return ChampionshipOutType.LIGUE_2;
        case PREMIER_LEAGUE:
            return ChampionshipOutType.PREMIER_LEAGUE;
        case LIGA:
            return ChampionshipOutType.LIGA;
        case SERIE_A:
            return ChampionshipOutType.SERIE_A;
        default:
            throw new UnsupportedOperationException(String.format("Championship type not supported: %s", championship));
        }
    }
}
