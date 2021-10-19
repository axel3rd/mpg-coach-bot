package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchTeamFormation {

    private String id;
    private int composition;
    private String captain;
    private SelectedBonus selectedBonus;

    public String getId() {
        return id;
    }

    public int getComposition() {
        return composition;
    }

    public String getCaptain() {
        return captain;
    }

    public SelectedBonus getSelectedBonus() {
        return selectedBonus;
    }
}
