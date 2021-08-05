package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchTeamFormation {

    private String id;
    private int composition;

    public String getId() {
        return id;
    }

    public int getComposition() {
        return composition;
    }
}
