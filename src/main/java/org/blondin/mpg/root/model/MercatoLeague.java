package org.blondin.mpg.root.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MercatoLeague implements Mercato {

    @JsonProperty("availablePlayers")
    private List<Player> players;

    public List<Player> getPlayers() {
        return players;
    }

}
