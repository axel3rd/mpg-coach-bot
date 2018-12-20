package org.blondin.mpg.root.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    @JsonProperty("playerid")
    private String id;
    @JsonProperty("firstname")
    private String firstName;
    @JsonProperty("lastname")
    private String lastName;
    private Position position;
    private int quotation;

    private double efficiency;

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return StringUtils.defaultIfBlank(firstName, "");
    }

    public String getLastName() {
        return StringUtils.defaultIfBlank(lastName, "");
    }

    public String getName() {
        return (getLastName() + " " + getFirstName()).trim();
    }

    public Position getPosition() {
        return position;
    }

    public int getQuotation() {
        return quotation;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

}
