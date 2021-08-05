package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DivisionLiveStat {

    private int currentGameWeek;
    private int totalGameWeeks;

    public int getCurrentGameWeek() {
        return currentGameWeek;
    }

    public int getTotalGameWeeks() {
        return totalGameWeeks;
    }
}
