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

    /**
     * Convenience method to remove ".0" suffix in the displayed float
     * 
     * @return Float
     */
    public Number getRating() {
        if (this.rating == (long) this.rating) {
            return (long) this.rating;
        }
        return this.rating;
    }

}
