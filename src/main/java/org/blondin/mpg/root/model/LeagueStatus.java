
package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LeagueStatus {

    CREATION(1), UNKNOWN(2), MERCATO(3), GAMES(4), TERMINATED(5);

    private final int value;

    private LeagueStatus(final int value) {
        this.value = value;
    }

    @JsonCreator
    public static LeagueStatus getNameByValue(final int value) {
        for (final LeagueStatus s : LeagueStatus.values()) {
            if (s.value == value) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("League status not supported: %s", value));
    }

}
