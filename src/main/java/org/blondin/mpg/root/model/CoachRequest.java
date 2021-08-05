package org.blondin.mpg.root.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoachRequest {

    @JsonProperty("playersOnPitch")
    private PlayersOnPitch playersOnPitch = new PlayersOnPitch();

    @JsonProperty("composition")
    private int composition;

    @JsonProperty("tacticalsubstitutes")
    private List<TacticalSubstitute> tacticalsubstitutes = new ArrayList<>();

    @JsonProperty("selectedBonus")
    private SelectedBonus bonusSelected = new SelectedBonus();

    public CoachRequest(int composition) {
        if (composition < 343 || composition > 541) {
            throw new UnsupportedOperationException(String.format("Invalid composition: %s", composition));
        }
        this.composition = composition;
    }

    public PlayersOnPitch getPlayersOnPitch() {
        return playersOnPitch;
    }

    public List<TacticalSubstitute> getTacticalsubstitutes() {
        return tacticalsubstitutes;
    }

    public SelectedBonus getBonusSelected() {
        return bonusSelected;
    }

    public void setBonusSelected(SelectedBonus bonusSelected) {
        this.bonusSelected = bonusSelected;
    }
}
