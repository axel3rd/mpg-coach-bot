
package org.blondin.mpg.root.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailablePlayers {

    @JsonProperty("availablePlayers")
    private List<Player> list;

    public List<Player> getList() {
        return list;
    }

}
