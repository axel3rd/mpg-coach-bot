
package org.blondin.mpg.stats.model;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Position {

    A(Arrays.asList("A")), M(Arrays.asList("M", "MD", "MO")), D(Arrays.asList("D", "DC", "DL")), G(Arrays.asList("G"));

    private final List<String> values;

    private Position(final List<String> values) {
        this.values = values;

    }

    @JsonCreator
    public static Position getNameByValue(final String value) {
        if (value == null) {
            return null;
        }
        for (final Position s : Position.values()) {
            if (s.values.contains(value)) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("Position not supported: %s", value));
    }

}
