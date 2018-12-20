package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class League {

    private String id;
    private String name;
    private ChampionshipType championship;
    private LeagueStatus leagueStatus;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ChampionshipType getChampionship() {
        return championship;
    }

    public LeagueStatus getLeagueStatus() {
        return leagueStatus;
    }
}
