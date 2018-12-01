package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BonusSelected {

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("playerid")
    @JsonInclude(Include.NON_NULL)
    private String playerId;

    public Integer getType() {
        return type;
    }

    public String getPlayerId() {
        return playerId;
    }
}
