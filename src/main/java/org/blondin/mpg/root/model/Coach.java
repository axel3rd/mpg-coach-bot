package org.blondin.mpg.root.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Coach {

    private String captain;

    @JsonProperty("matchTeamFormation")
    private MatchTeamFormation matchTeamFormation;

    @JsonProperty("selectedBonus")
    private BonusSelected bonusSelected;

    // Useless below parameters ?

    @JsonProperty("players")
    private List<Player> players;

    @JsonProperty("bonus")
    private Bonus bonus;

    @JsonProperty("matchId")
    private String matchId;

    /** Number of players in the league */
    @JsonProperty("nbPlayers")
    private int nbPlayers;

    @JsonProperty("realday")
    private int realDay;

    @JsonProperty("teams")
    private Map<Integer, Club> teams;

    public int getComposition() {
        return matchTeamFormation.getComposition();
    }

    public BonusSelected getBonusSelected() {
        return bonusSelected;
    }

    // Useless below functions ?

    public List<Player> getPlayers() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return players;
    }

    public Bonus getBonus() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return bonus;
    }

    public String getMatchId() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return matchId;
    }

    public int getNbPlayers() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return nbPlayers;
    }

    public int getRealDay() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return realDay;
    }

    public Map<Integer, Club> getTeams() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return teams;
    }

}
