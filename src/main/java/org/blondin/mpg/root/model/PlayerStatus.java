
package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PlayerStatus {

    BUY(2), PROPOSAL(1);

    private final int value;

    private PlayerStatus(final int value) {
        this.value = value;
    }

    @JsonCreator
    public static PlayerStatus getNameByValue(final int value) {
        for (final PlayerStatus s : PlayerStatus.values()) {
            if (s.value == value) {
                return s;
            }
        }
        throw new UnsupportedOperationException(String.format("Player status not supported: %s", value));
    }

}
