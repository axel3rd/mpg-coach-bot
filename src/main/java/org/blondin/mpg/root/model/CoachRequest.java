package org.blondin.mpg.root.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoachRequest {

    @JsonProperty("playersOnPitch")
    private PlayersOnPitch playersOnPitch = new PlayersOnPitch();

    @JsonProperty("composition")
    private int composition;

    @JsonProperty("tacticalsubstitutes")
    private List<TacticalSubstitute> tacticalsubstitutes = new ArrayList<>();

    @JsonProperty("bonusSelected")
    private BonusSelected bonusSelected = new BonusSelected();

    @JsonProperty("matchId")
    private String matchId;

    @JsonProperty("realday")
    private int realDay;

    public CoachRequest(Coach coach) {
        if (coach.getComposition() < 343 || coach.getComposition() > 541) {
            throw new UnsupportedOperationException(String.format("Invalid composition: %s", coach.getComposition()));
        }
        if (coach.getRealDay() < 1) {
            throw new UnsupportedOperationException(String.format("Invalid real day: %s", coach.getRealDay()));
        }
        this.composition = coach.getComposition();
        this.matchId = coach.getMatchId();
        this.realDay = coach.getRealDay();
        this.bonusSelected = ObjectUtils.defaultIfNull(coach.getBonusSelected(), new BonusSelected());
    }

    public PlayersOnPitch getPlayersOnPitch() {
        return playersOnPitch;
    }

    public List<TacticalSubstitute> getTacticalsubstitutes() {
        return tacticalsubstitutes;
    }

    public BonusSelected getBonusSelected() {
        return bonusSelected;
    }

}
