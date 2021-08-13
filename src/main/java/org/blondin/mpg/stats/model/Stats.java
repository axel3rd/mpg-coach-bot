
package org.blondin.mpg.stats.model;

import java.util.Map;

import org.apache.commons.math3.util.Precision;

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

    @JsonProperty("Oa")
    private double oldAverage;
    @JsonProperty("Og")
    private int oldGoals;
    @JsonProperty("On")
    private int oldMatchs;

    @JsonProperty("p")
    private Map<Integer, StatsDay> statsDay;

    private int currentSeasonDay;
    private int previousMaxSeasonDay;

    /**
     * Goals on last days (or season if parameter not set)
     * 
     * @param days Number of days to retrieve statistic (or season if <= 0)
     * @return number of goals
     */
    public int getGoals(int days) {
        if (currentSeasonDay <= 0 && days <= 0) {
            return oldGoals;
        }
        if (days <= 0) {
            return goals;
        }
        if (statsDay == null) {
            return 0;
        }
        int nbrGoals = 0;
        int maxDay = currentSeasonDay > 0 ? currentSeasonDay : previousMaxSeasonDay;
        for (int d = maxDay; d > (maxDay - days); d--) {
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
        if (currentSeasonDay <= 0 && days <= 0) {
            return oldAverage;
        }
        if (days <= 0) {
            return average;
        }
        if (statsDay == null) {
            return 0;
        }
        double averageLastDaysSum = 0;
        int daysPlayed = 0;
        int maxDay = currentSeasonDay > 0 ? currentSeasonDay : previousMaxSeasonDay;
        for (int d = maxDay; d > (maxDay - days); d--) {
            if (statsDay.containsKey(d)) {
                averageLastDaysSum += statsDay.get(d).getAverage();
                daysPlayed++;
            }
        }
        if (daysPlayed == 0) {
            return 0;
        }
        return Precision.round(averageLastDaysSum / daysPlayed, 2);
    }

    /**
     * Matchs played on last days (or season if parameter not set)
     * 
     * @param days Number of days to retrieve statistic (or season if <= 0)
     * @return number of matchs
     */
    public int getMatchs(int days) {
        if (currentSeasonDay <= 0 && days <= 0) {
            return oldMatchs;
        }
        if (days <= 0) {
            return matchs;
        }
        if (statsDay == null) {
            return 0;
        }
        int nbrMatchs = 0;
        int maxDay = currentSeasonDay > 0 ? currentSeasonDay : previousMaxSeasonDay;
        for (int d = maxDay; d > (maxDay - days); d--) {
            if (statsDay.containsKey(d)) {
                nbrMatchs++;
            }
        }
        return nbrMatchs;
    }

    void setCurrentSeasonDay(int currentSeasonDay) {
        this.currentSeasonDay = currentSeasonDay;
    }

    void setPreviousMaxSeasonDay(int previousMaxSeasonDay) {
        this.previousMaxSeasonDay = previousMaxSeasonDay;
    }

    Map<Integer, StatsDay> getStatsDay() {
        return statsDay;
    }

    void setStatsDay(Map<Integer, StatsDay> statsDay) {
        this.statsDay = statsDay;
    }
}
