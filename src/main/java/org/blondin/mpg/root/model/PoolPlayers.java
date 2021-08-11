package org.blondin.mpg.root.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PoolPlayers {

    @JsonProperty("poolPlayers")
    private List<Player> players;

    public List<Player> getPlayers() {
        return players;
    }

    public Player getPlayer(String id) {
        return getPlayers().stream().filter(p -> p.getId().equals(id)).findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(String.format("Player with id '%s' cannot found", id)));
    }
}
