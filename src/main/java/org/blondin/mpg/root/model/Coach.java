package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Coach {

    @JsonProperty("matchTeamFormation")
    private MatchTeamFormation matchTeamFormation;

    public String getIdMatch() {
        return matchTeamFormation.getId();
    }

    public int getComposition() {
        return matchTeamFormation.getComposition();
    }

    public String getCaptain() {
        return matchTeamFormation.getCaptain();
    }

    public SelectedBonus getBonusSelected() {
        return matchTeamFormation.getSelectedBonus();
    }

}
