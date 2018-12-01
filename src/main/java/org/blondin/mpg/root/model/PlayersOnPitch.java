package org.blondin.mpg.root.model;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayersOnPitch {

    @JsonProperty("1")
    private String playerId1;

    @JsonProperty("2")
    private String playerId2;

    @JsonProperty("3")
    private String playerId3;

    @JsonProperty("4")
    private String playerId4;

    @JsonProperty("5")
    private String playerId5;

    @JsonProperty("6")
    private String playerId6;

    @JsonProperty("7")
    private String playerId7;

    @JsonProperty("8")
    private String playerId8;

    @JsonProperty("9")
    private String playerId9;

    @JsonProperty("10")
    private String playerId10;

    @JsonProperty("11")
    private String playerId11;

    @JsonProperty("12")
    private String playerId12;

    @JsonProperty("13")
    private String playerId13;

    @JsonProperty("14")
    private String playerId14;

    @JsonProperty("15")
    private String playerId15;

    @JsonProperty("16")
    private String playerId16;

    @JsonProperty("17")
    private String playerId17;

    @JsonProperty("18")
    private String playerId18;

    public void setPlayer(int position, String playerId) {
        if (position < 1 || position > 18) {
            throw new UnsupportedOperationException(String.format("Invalid position: %s", position));
        }
        try {
            FieldUtils.writeDeclaredField(this, "playerId" + position, playerId, true);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(
                    String.format("Can't set playerId '%s' on pitch for position %s: %s", playerId, position, e.getMessage()), e);
        }
    }

    public String getPlayer(int position) {
        try {
            return (String) FieldUtils.readDeclaredField(this, "playerId" + position, true);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(String.format("Can't get playerId on pitch for position %s: %s", position, e.getMessage()), e);
        }
    }

}
