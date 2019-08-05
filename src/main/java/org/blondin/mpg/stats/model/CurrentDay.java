package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentDay {

    @JsonProperty("d")
    private int day;

    public int getDay() {
        return day;
    }
}
