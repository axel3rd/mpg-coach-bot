package org.blondin.mpg.root.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Clubs {

    private Map<String, Club> championshipClubs;

    public Map<String, Club> getChampionshipClubs() {
        return championshipClubs;
    }
}
