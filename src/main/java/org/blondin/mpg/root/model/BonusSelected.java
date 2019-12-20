package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BonusSelected {

    @JsonProperty("type")
    @JsonInclude(Include.NON_NULL)
    private Integer type;

    @JsonProperty("playerid")
    @JsonInclude(Include.NON_NULL)
    private String playerId;

    public Integer getType() {
        if (type != null && type == 0) {
            return null;
        }
        return type;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
