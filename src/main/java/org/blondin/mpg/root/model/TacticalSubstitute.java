package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TacticalSubstitute {

    @JsonProperty("subs")
    private String playerIdSubstitute;

    @JsonProperty("start")
    private String playerIdStart;

    @JsonProperty("rating")
    private float rating;

    public TacticalSubstitute(String playerIdSubstitute, String playerIdStart, float rating) {
        this.playerIdSubstitute = playerIdSubstitute;
        this.playerIdStart = playerIdStart;
        this.rating = rating;
    }

}
