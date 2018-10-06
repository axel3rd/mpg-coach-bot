
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
    @JsonProperty("n")
    private int matchs;

    public int getGoals() {
        return goals;
    }

    /**
     * Average notation on season
     * 
     * @return note
     */
    public float getAverage() {
        return average;
    }

    /**
     * Matchs played in current season
     * 
     * @return number of matchs
     */
    public int getMatchs() {
        return matchs;
    }

}
