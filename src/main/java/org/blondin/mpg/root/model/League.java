package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class League {

    private String id;
    private String name;
    private ChampionshipType championship;
    private LeagueStatus leagueStatus;
    private int teamStatus;
    private int players;

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

    public int getTeamStatus() {
        return teamStatus;
    }

    public int getPlayers() {
        return players;
    }
}
