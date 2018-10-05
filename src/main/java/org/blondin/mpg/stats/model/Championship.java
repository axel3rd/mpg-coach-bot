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

    public Player getPlayer(String name) {
        for (Player p : getPlayers()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        throw new UnsupportedOperationException(String.format("Player can't be found in stats: %s", name));
    }
}
