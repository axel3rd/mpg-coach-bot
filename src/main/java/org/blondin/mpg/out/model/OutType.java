
package org.blondin.mpg.out.model;

import java.util.Arrays;
import java.util.List;

public enum OutType {

    INJURY_GREEN("injury_green"), INJURY_ORANGE("injury_orange"), INJURY_RED("injury", "cross"), SUSPENDED("suspended", "red"), ASBENT("absent");

    private final List<String> values;

    private OutType(final String... values) {
        this.values = Arrays.asList(values);
    }

    public static OutType getNameByValue(final String value) {
        for (final OutType s : OutType.values()) {
            if (s.values.contains(value)) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("Injury/Suspended not supported: %s", value));
    }

}
