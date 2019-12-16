package org.blondin.mpg.root.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "data")
public class Coach {

    @JsonProperty("players")
    private List<Player> players;

    @JsonProperty("composition")
    private int composition;

    @JsonProperty("bonus")
    private Bonus bonus;

    @JsonProperty("bonusSelected")
    private BonusSelected bonusSelected;

    @JsonProperty("matchId")
    private String matchId;

    /** Number of players in the league */
    @JsonProperty("nbPlayers")
    private int nbPlayers;

    @JsonProperty("realday")
    private int realDay;

    @JsonProperty("teams")
    private Map<Integer, Team> teams;

    public List<Player> getPlayers() {
        return players;
    }

    public int getComposition() {
        return composition;
    }

    public Bonus getBonus() {
        return bonus;
    }

    public BonusSelected getBonusSelected() {
        return bonusSelected;
    }

    public String getMatchId() {
        return matchId;
    }

    public int getNbPlayers() {
        return nbPlayers;
    }

    public int getRealDay() {
        return realDay;
    }

    public Map<Integer, Team> getTeams() {
        return teams;
    }

}
