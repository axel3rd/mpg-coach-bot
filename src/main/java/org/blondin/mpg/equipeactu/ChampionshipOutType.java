
package org.blondin.mpg.equipeactu;

public enum ChampionshipOutType {

    LIGUE_1("france/ligue-1"), LIGUE_2("france/ligue-2"), PREMIER_LEAGUE("angleterre/championship"), LIGA("espagne/primera-division"),
    SERIE_A("italie/serie-a");

    private final String value;

    private ChampionshipOutType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
