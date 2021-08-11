package org.blondin.mpg.root.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class League {

    @JsonProperty("leagueId")
    private String leagueId;
    private String divisionId;
    private int divisionTotalUsers;
    private String name;
    @JsonProperty("championshipId")
    private ChampionshipType championship;
    private LeagueStatus status;
    private Mode mode;
    private int currentTeamStatus;

    public String getId() {
        return StringUtils.removeStart(leagueId, "mpg_league_");
    }

    public String getDivisionId() {
        return divisionId;
    }

    public int getDivisionTotalUsers() {
        return divisionTotalUsers;
    }

    public String getName() {
        return name;
    }

    public ChampionshipType getChampionship() {
        return championship;
    }

    public LeagueStatus getStatus() {
        return status;
    }

    public Mode getMode() {
        return mode;
    }

    public int getCurrentTeamStatus() {
        return currentTeamStatus;
    }

}
