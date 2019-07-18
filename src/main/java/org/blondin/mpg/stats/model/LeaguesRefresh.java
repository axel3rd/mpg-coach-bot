package org.blondin.mpg.stats.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LeaguesRefresh {

    @JsonProperty("1")
    private Date date1;

    @JsonProperty("2")
    private Date date2;

    @JsonProperty("3")
    private Date date3;

    @JsonProperty("4")
    private Date date4;

    @JsonProperty("5")
    private Date date5;

    public Date getDate(int league) {
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
