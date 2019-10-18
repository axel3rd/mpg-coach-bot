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
        if (this.isMasterLeague) {
            return subLeague.getId();
        }
        return id;
    }

    public String getName() {
        if (this.isMasterLeague) {
            return subLeague.getName();
        }
        return name;
    }

    public ChampionshipType getChampionship() {
        if (this.isMasterLeague) {
            return subLeague.getChampionship();
        }
        return championship;
    }

    public LeagueStatus getLeagueStatus() {
        if (this.isMasterLeague) {
            return subLeague.getLeagueStatus();
        }
        return leagueStatus;
    }

    public Mode getMode() {
        if (this.isMasterLeague) {
            return subLeague.getMode();
        }
        return mode;
    }

    public int getTeamStatus() {
        if (this.isMasterLeague) {
            return subLeague.getTeamStatus();
        }
        return teamStatus;
    }

}
