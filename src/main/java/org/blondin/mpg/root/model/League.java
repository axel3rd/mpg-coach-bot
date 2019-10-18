package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class League {

    private String id;
    private String name;
    private ChampionshipType championship;
    private LeagueStatus leagueStatus;
    private Mode mode;
    private int teamStatus;
    @JsonProperty("isMasterLeague")
    private boolean isMasterLeague;
    @JsonProperty("league")
    private League subLeague;

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

    public Mode getMode() {
        return mode;
    }

    public int getTeamStatus() {
        return teamStatus;
    }

    public boolean isMasterLeague() {
        return isMasterLeague;
    }

    public League getSubLeague() {
        return subLeague;
    }
}
