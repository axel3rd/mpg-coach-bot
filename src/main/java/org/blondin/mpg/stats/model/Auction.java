package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Auction {

    @JsonProperty("m")
    private int min;
    @JsonProperty("a")
    private int average;
    @JsonProperty("M")
    private int max;
    @JsonProperty("n")
    private int number;

    public int getMin() {
        return min;
    }

    public int getAverage() {
        return average;
    }

    public int getMax() {
        return max;
    }

    public int getNumber() {
        return number;
    }

}
