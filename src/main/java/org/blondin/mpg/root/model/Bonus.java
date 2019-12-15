
package org.blondin.mpg.root.model;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bonus {

    @JsonProperty("1")
    private int bonus1;

    @JsonProperty("2")
    private int bonus2;

    @JsonProperty("3")
    private int bonus3;

    @JsonProperty("4")
    private int bonus4;

    @JsonProperty("5")
    private int bonus5;

    @JsonProperty("6")
    private int bonus6;

    @JsonProperty("7")
    private int bonus7;

    public int getNumber() {
        int nbr = 0;
        for (int i = 1; i <= 7; i++) {
            try {
                nbr += (int) FieldUtils.readDeclaredField(this, "bonus" + i, true);
            } catch (IllegalAccessException e) {
                throw new UnsupportedOperationException(String.format("Can't get bonus %s: %s", i, e.getMessage()), e);
            }
        }
        return nbr;
    }

    public int getBonusTypeForRemainingMatch(int matchsRemaining) {
        int type = 0;
        int remaining = matchsRemaining;
        for (int i = 1; i <= 7; i++) {
            type = i;
            try {
                int nbr = (int) FieldUtils.readDeclaredField(this, "bonus" + i, true);
                for (int j = 0; j < nbr; j++) {
                    remaining -= 1;
                }
                if (remaining <= 0) {
                    break;
                }
            } catch (IllegalAccessException e) {
                throw new UnsupportedOperationException(String.format("Can't get bonus %s: %s", i, e.getMessage()), e);
            }
        }
        if (type < 1 || type > 7) {
            throw new UnsupportedOperationException("Bonus type should be between 1 and 7");
        }
        return type;
    }
}
