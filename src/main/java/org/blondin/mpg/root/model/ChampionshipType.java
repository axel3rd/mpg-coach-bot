
package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ChampionshipType {

    LIGUE_1(1), PREMIER_LEAGUE(2), LIGA(3), LIGUE_2(4), SERIE_A(5), CHAMPIONS_LEAGUE(6), LIGUE_SUPER(7);

    private final int value;

    private ChampionshipType(final int value) {
        this.value = value;
    }

    @JsonCreator
    public static ChampionshipType getNameByValue(final int value) {
        for (final ChampionshipType s : ChampionshipType.values()) {
            if (s.value == value) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("Championship type not supported: %s", value));
    }

    public int value() {
        return this.value;
    }
}
