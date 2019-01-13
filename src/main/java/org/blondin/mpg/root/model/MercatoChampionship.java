package org.blondin.mpg.root.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MercatoChampionship implements Mercato {

    @JsonProperty("players")
    private List<Player> players;

    public List<Player> getPlayers() {
        return players;
    }

}
