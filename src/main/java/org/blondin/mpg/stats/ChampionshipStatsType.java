
package org.blondin.mpg.stats;

public enum ChampionshipStatsType {

    LIGUE_1("Ligue-1"), LIGUE_2("Ligue-2"), PREMIER_LEAGUE("Premier-League"), LIGA("Liga"), SERIE_A("SerieA");

    private final String value;

    private ChampionshipStatsType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
