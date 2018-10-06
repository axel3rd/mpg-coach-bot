package org.blondin.mpg.stats.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Player
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    @JsonProperty("f")
    private String firstName;
    @JsonProperty("n")
    private String lastName;
    @JsonProperty("s")
    private Stats stats;
    @JsonProperty("r")
    private int price;

    private double efficiency;

    public String getFirstName() {
        return StringUtils.defaultIfBlank(firstName, "");
    }

    public String getLastName() {
        return StringUtils.defaultIfBlank(lastName, "");
    }

    public String getName() {
        return (getLastName() + " " + getFirstName()).trim();
    }

    public int getPrice() {
        return price;
    }

    public Stats getStats() {
        return stats;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }
}
