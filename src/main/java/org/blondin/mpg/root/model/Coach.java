package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Coach {

    private String captain;

    @JsonProperty("matchTeamFormation")
    private MatchTeamFormation matchTeamFormation;

    @JsonProperty("selectedBonus")
    private SelectedBonus bonusSelected;

    public String getIdMatch() {
        return matchTeamFormation.getId();
    }

    public int getComposition() {
        return matchTeamFormation.getComposition();
    }

    public SelectedBonus getBonusSelected() {
        return bonusSelected;
    }

}
