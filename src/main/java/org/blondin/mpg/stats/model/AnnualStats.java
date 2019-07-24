package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnualStats {

    @JsonProperty("mD")
    private int maxDay;

    public int getMaxDay() {
        return maxDay;
    }
}
