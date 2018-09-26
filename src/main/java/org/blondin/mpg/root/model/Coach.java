/*
 * Creation : 25 sept. 2018
 */
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

    public List<Player> getPlayers() {
        return players;
    }

}
