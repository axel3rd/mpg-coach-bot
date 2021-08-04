package org.blondin.mpg.root.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    private String id;
    private String firstName;
    private String lastName;
    private Position position;
    private int quotation;
    @JsonProperty("price")
    private int pricePaid;

    @JsonProperty("teamid")
    private int teamId;
    @JsonProperty("club")
    private String club;
    private PlayerStatus status;

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

    public int getPricePaid() {
        return pricePaid;
    }

    public int getTeamId() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be checked");
        }
        return teamId;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public String getClub() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be checked");
        }
        return club;
    }

    public void setClub(String club) {
        this.club = club;
    }

    public PlayerStatus getStatus() {
        if (true) {
            throw new UnsupportedOperationException("Usage should be checked");
        }
        return status;
    }

}
