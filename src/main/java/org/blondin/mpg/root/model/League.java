package org.blondin.mpg.root.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class League {

    @JsonProperty("leagueId")
    private String leagueId;
    private String divisionId;
    private String name;
    @JsonProperty("championshipId")
    private ChampionshipType championship;
    private LeagueStatus status;
    private Mode mode;
    private int teamStatus;
    @JsonProperty("isMasterLeague")
    private boolean isMasterLeague;
    @JsonProperty("league")
    private League subLeague;

    public String getId() {
        return StringUtils.removeStart(leagueId, "mpg_league_");
    }

    public String getDivisionId() {
        return divisionId;
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

    public int getTeamStatus() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return teamStatus;
    }

    public boolean isMasterLeague() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return isMasterLeague;
    }

    public League getSubLeague() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return subLeague;
    }
}
