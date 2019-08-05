package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SeasonStats {

    @JsonProperty("mD")
    private int maxDay;

    @JsonProperty("cD")
    private CurrentDay currentDay;

    public int getMaxDay() {
        return maxDay;
    }

    public CurrentDay getCurrentDay() {
        return currentDay;
    }
}
