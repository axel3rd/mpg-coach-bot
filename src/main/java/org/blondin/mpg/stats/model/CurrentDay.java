package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentDay {

    @JsonProperty("d")
    private int day;

    @JsonProperty("lD")
    private int lastDayReached;

    @JsonProperty("p")
    private int played;

    public int getDay() {
        return day;
    }

    public int getLastDayReached() {
        return lastDayReached;
    }

    public int getPlayed() {
        return played;
    }

}
