
package org.blondin.mpg.root.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferBuy {

    private int budget;
    private List<Player> availablePlayers;
    private List<Player> userPlayers;

    public int getBudget() {
        return budget;
    }

    public List<Player> getAvailablePlayers() {
        return availablePlayers;
    }

    public List<Player> getUserPlayers() {
        return userPlayers;
    }
}
