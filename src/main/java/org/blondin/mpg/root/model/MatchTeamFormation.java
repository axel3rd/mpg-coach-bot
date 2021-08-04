package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchTeamFormation {

    private int composition;

    public int getComposition() {
        return composition;
    }
}
