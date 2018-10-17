
package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ChampionshipType {

    LIGUE_1(1), PREMIER_LEAGUE(2), LIGA(3);

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

}
