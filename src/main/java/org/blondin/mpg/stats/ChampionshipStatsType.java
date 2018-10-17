
package org.blondin.mpg.stats;

public enum ChampionshipStatsType {

    LIGUE_1("Ligue-1"), PREMIER_LEAGUE("Premier-League"), LIGA("Liga");

    private final String value;

    private ChampionshipStatsType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
