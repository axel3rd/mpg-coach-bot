package org.blondin.mpg.root.model;

import java.util.List;

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

    @JsonProperty("bonusSelected")
    private BonusSelected bonusSelected;

    @JsonProperty("matchId")
    private String matchId;

    @JsonProperty("realday")
    private int realDay;

    public List<Player> getPlayers() {
        return players;
    }

    public int getComposition() {
        return composition;
    }

    public BonusSelected getBonusSelected() {
        return bonusSelected;
    }

    public String getMatchId() {
        return matchId;
    }

    public int getRealDay() {
        return realDay;
    }

}
