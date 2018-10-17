
package org.blondin.mpg.equipeactu;

public enum ChampionshipOutType {

    LIGUE_1("france/ligue-1"), PREMIER_LEAGUE("angleterre/championship"), LIGA("espagne/primera-division");

    private final String value;

    private ChampionshipOutType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
