package org.blondin.mpg.root.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CoachRequest {
    @JsonProperty("composition")
    private String composition;

    @JsonProperty("playersOnPitch")
    private PlayersOnPitch playersOnPitch = new PlayersOnPitch();

    @JsonProperty("tacticalSubs")
    private List<TacticalSubstitute> tacticalSubstitutes = new ArrayList<>();

    @JsonProperty("selectedBonus")
    @JsonInclude(Include.NON_NULL)
    private SelectedBonus bonusSelected;

    public CoachRequest(int composition) {
        if (composition < 343 || composition > 541) {
            throw new UnsupportedOperationException(String.format("Invalid composition: %s", composition));
        }
        this.composition = String.valueOf(composition);
    }

    public PlayersOnPitch getPlayersOnPitch() {
        return playersOnPitch;
    }

    public List<TacticalSubstitute> getTacticalSubstitutes() {
        return tacticalSubstitutes;
    }

    public SelectedBonus getBonusSelected() {
        return bonusSelected;
    }

    public void setBonusSelected(SelectedBonus bonusSelected) {
        this.bonusSelected = bonusSelected;
    }
}
