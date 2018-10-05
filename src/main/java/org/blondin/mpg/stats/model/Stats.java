
package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Statistics for a player
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stats {

    @JsonProperty("a")
    private float average;
    @JsonProperty("g")
    private int goals;

    public int getGoals() {
        return goals;
    }

    public float getAverage() {
        return average;
    }

}
