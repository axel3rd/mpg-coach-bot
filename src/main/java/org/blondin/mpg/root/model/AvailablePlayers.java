
package org.blondin.mpg.root.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailablePlayers {

    private List<Player> availablePlayers;

    public List<Player> getAvailablePlayers() {
        return availablePlayers;
    }

}
