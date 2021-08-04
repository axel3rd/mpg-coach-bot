package org.blondin.mpg.root.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {

    private String name;
    private int budget;
    private Map<String, Player> squad;

    public String getName() {
        return name;
    }

    public int getBudget() {
        return budget;
    }

    public Map<String, Player> getSquad() {
        return squad;
    }
}
