
package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Mode {

    NORMAL(1), EXPERT(2);

    private final int value;

    private Mode(final int value) {
        this.value = value;
    }

    @JsonCreator
    public static Mode getNameByValue(final int value) {
        for (final Mode s : Mode.values()) {
            if (s.value == value) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("League mode not supported: %s", value));
    }

}
