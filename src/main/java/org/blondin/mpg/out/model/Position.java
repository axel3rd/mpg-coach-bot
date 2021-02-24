
package org.blondin.mpg.out.model;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Position {

    A("(AT)", "(F)"), M("(MI)", "(M)"), D("(DF)", "(D)"), G("(GK)", "(G)"), UNDEFINED("");

    private final List<String> values;

    private Position(final String... values) {
        this.values = Arrays.asList(values);
    }

    @JsonCreator
    public static Position getNameByValue(final String value) {
        for (final Position s : Position.values()) {
            if (s.values.contains(value)) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("Position not supported: %s", value));
    }

}
