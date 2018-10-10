
package org.blondin.mpg.equipeactu.model;

public enum OutType {

    INJURY_GREEN("injury_green"), INJURY_ORANGE("injury_orange"), INJURY_RED("injury"), SUSPENDED("suspended");

    private final String value;

    private OutType(final String value) {
        this.value = value;
    }

    public static OutType getNameByValue(final String value) {
        for (final OutType s : OutType.values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("Injury/Suspended not supported: %s", value));
    }

}
