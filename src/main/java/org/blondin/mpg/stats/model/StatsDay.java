package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Statistics for a player day
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatsDay {

    @JsonProperty("n")
    private double average;
    @JsonProperty("g")
    private int goals;

    public double getAverage() {
        return average;
    }

    public int getGoals() {
        return goals;
    }
}
