package org.blondin.mpg.root.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dashboard {

    @JsonProperty("leaguesDivisionsItems")
    private List<League> leagues;

    public List<League> getLeagues() {
        return leagues;
    }
}
