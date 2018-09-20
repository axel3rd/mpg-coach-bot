package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Player
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    @JsonProperty("n")
    private String firstName;
    @JsonProperty("f")
    private String lastName;
}
