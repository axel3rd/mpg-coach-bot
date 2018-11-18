package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TacticalSubstitute {

    @JsonProperty("subs")
    private String playerIdSubstitute;

    @JsonProperty("start")
    private String playerIdStart;

    @JsonProperty("rating")
    private float rating;

}
