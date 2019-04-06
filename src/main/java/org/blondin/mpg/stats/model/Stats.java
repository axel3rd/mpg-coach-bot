
package org.blondin.mpg.stats.model;

import java.util.Map;

import org.apache.commons.math.util.MathUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Statistics for a player
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stats {

    @JsonProperty("a")
    private double average;
    @JsonProperty("g")
    private int goals;
    @JsonProperty("n")
    private int matchs;
    @JsonProperty("p")
    private Map<Integer, StatsDay> statsDay;

    private int currentSeasonDay;

    /**
     * Goals on last days (or season if parameter not set)
     * 
     * @param days Number of days to retrieve statistic (or season if <= 0)
     * @return number of goals
     */
    public int getGoals(int days) {
        if (days <= 0 || statsDay == null) {
            return goals;
        }
        int nbrGoals = 0;
        for (int d = currentSeasonDay; d > (currentSeasonDay - days); d--) {
            if (statsDay.containsKey(d)) {
                nbrGoals += statsDay.get(d).getGoals();
            }
        }
        return nbrGoals;
    }

    /**
     * Average notation on last days (or season if parameter not set)
     * 
     * @param days Number of days to retrieve statistic (or season if <= 0)
     * @return note
     */
    public double getAverage(int days) {
        if (days <= 0 || statsDay == null) {
            return average;
        }
        double averageLastDaysSum = 0;
        for (int d = currentSeasonDay; d > (currentSeasonDay - days); d--) {
            if (statsDay.containsKey(d)) {
                averageLastDaysSum += statsDay.get(d).getAverage();
            }
        }
        return MathUtils.round(averageLastDaysSum / days, 2);
    }

    /**
     * Matchs played on last days (or season if parameter not set)
     * 
     * @param days Number of days to retrieve statistic (or season if <= 0)
     * @return number of matchs
     */
    public int getMatchs(int days) {
        if (days <= 0 || statsDay == null) {
            return matchs;
        }
        int nbrMatchs = 0;
        for (int d = currentSeasonDay; d > (currentSeasonDay - days); d--) {
            if (statsDay.containsKey(d)) {
                nbrMatchs++;
            }
        }
        return nbrMatchs;
    }

    void setCurrentSeasonDay(int currentSeasonDay) {
        this.currentSeasonDay = currentSeasonDay;
    }
}
