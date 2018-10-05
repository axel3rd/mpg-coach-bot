
package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Position {

    A(4), M(3), D(2), G(1);

    private final int value;

    Position(final int value) {
        this.value = value;
    }

    @JsonCreator
    public static Position getNameByValue(final int value) {
        for (final Position s : Position.values()) {
            if (s.value == value) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("Position not supported: %s", value));
    }

}
