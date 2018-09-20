package org.blondin.mpg.stats.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Championship (mpgstats)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Championship {

    @JsonProperty("p")
    private List<Player> players;

    public List<Player> getPlayers() {
        return players;
    }
}
