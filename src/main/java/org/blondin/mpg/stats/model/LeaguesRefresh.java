package org.blondin.mpg.stats.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LeaguesRefresh {

    @JsonProperty("1")
    private Instant date1;

    @JsonProperty("2")
    private Instant date2;

    @JsonProperty("3")
    private Instant date3;

    @JsonProperty("4")
    private Instant date4;

    @JsonProperty("5")
    private Instant date5;

    public Instant getDate(int league) {
        switch (league) {
        case 1:
            return date1;
        case 2:
            return date2;
        case 3:
            return date3;
        case 4:
            return date4;
        case 5:
            return date5;
        default:
            throw new UnsupportedOperationException(String.format("League not supported: %s", league));
        }
    }
}
