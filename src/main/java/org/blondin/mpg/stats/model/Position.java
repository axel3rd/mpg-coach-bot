
package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Position {

    A("A"), M("M"), D("D"), G("G");

    private final String value;

    private Position(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static Position getNameByValue(final String value) {
        for (final Position s : Position.values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("Position not supported: %s", value));
    }

}
