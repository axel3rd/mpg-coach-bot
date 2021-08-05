package org.blondin.mpg.root.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {

    private String name;
    private int budget;
    private Map<String, Player> squad;
    private Map<String, Integer> bonuses;

    public Map<String, Integer> getBonuses() {
        return bonuses;
    }

    public int getBonusesNumber() {
        return bonuses.values().stream().reduce(0, Integer::sum);
    }

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
