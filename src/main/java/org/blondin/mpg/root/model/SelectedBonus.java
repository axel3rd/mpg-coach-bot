package org.blondin.mpg.root.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * On mobile App:<br>
 * - Wallet: removeGoal<br>
 * - Zahia: boostAllPlayers<br>
 * - Suarez: nerfGoalkeeper<br>
 * - UberEats: boostOnePlayer ('playerId' required)<br>
 * - Mirror: mirror<br>
 * - Pat: blockTacticalSubs<br>
 * - Chapron: removeRandomPlayer<br>
 * -
 */
public class SelectedBonus {

    private static final List<String> BONUS_PRIORITY = Stream
            .of("removeGoal", "boostAllPlayers", "nerfGoalkeeper", "boostOnePlayer", "mirror", "blockTacticalSubs", "removeRandomPlayer")
            .collect(Collectors.toList());

    @JsonInclude(Include.NON_NULL)
    private String name;

    @JsonInclude(Include.NON_NULL)
    private String playerId;

    public static List<String> getBonusPriority() {
        return BONUS_PRIORITY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

}
