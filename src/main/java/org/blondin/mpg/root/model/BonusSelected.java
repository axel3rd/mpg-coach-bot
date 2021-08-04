package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * On mobile App:<br>
 * - Wallet: removeGoal<br>
 * - UberEats: boostOnePlayer ('playerId' required)<br>
 * - Suarez: nerfGoalkeeper<br>
 * - Pat: blockTacticalSubs<br>
 * - Zahia: boostAllPlayers<br>
 * - Mirror: mirror<br>
 * - Chapron: removeRandomPlayer<br>
 * -
 */
public class BonusSelected {

    // TODO: Order bonus by priority !!

    @JsonInclude(Include.NON_NULL)
    private String name;

    @JsonInclude(Include.NON_NULL)
    private String playerId;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Integer getType() {
        throw new UnsupportedOperationException("Now it is name");
    }

    public void setType(int t) {
        throw new UnsupportedOperationException("Now it is name");
    }
}
