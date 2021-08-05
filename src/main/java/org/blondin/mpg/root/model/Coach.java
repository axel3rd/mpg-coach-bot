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
    private SelectedBonus bonusSelected;

    // Useless below parameters ?

    @JsonProperty("players")
    private List<Player> players;

    /** Number of players in the league */
    @JsonProperty("nbPlayers")
    private int nbPlayers;

    @JsonProperty("teams")
    private Map<Integer, Club> teams;

    public int getComposition() {
        return matchTeamFormation.getComposition();
    }

    public SelectedBonus getBonusSelected() {
        return bonusSelected;
    }

    // Useless below functions ?

    public List<Player> getPlayers() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return players;
    }

    public int getNbPlayers() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return nbPlayers;
    }

    public Map<Integer, Club> getTeams() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be analysed");
        }
        return teams;
    }

}
