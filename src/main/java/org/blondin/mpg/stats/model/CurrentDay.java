package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentDay {

    @JsonProperty("d")
    private int day;

    @JsonProperty("lD")
    private int lastDayReached;

    public int getDay() {
        return day;
    }

    public int getDayReached() {
        return lastDayReached;
    }

    public boolean isStatsDayReached() {
        return day == lastDayReached;
    }

}
