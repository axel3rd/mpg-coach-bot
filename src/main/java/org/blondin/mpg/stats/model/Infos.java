package org.blondin.mpg.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Infos {

    @JsonProperty("cN")
    private String name;
    @JsonProperty("i")
    private int id;
    @JsonProperty("aS")
    private AnnualStats annualStats;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public AnnualStats getAnnualStats() {
        return annualStats;
    }
}
