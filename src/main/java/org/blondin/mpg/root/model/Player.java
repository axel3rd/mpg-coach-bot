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
    private String clubId;
    private String clubName;
    private double efficiency;
    private int auction;

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

    public int getAuction() {
        return auction;
    }

    public void setAuction(int auction) {
        this.auction = auction;
    }

    public void setPricePaid(int pricePaid) {
        this.pricePaid = pricePaid;
    }

    public String getClubId() {
        return clubId;
    }

    public String getClubName() {
        if (StringUtils.isBlank(clubName)) {
            throw new UnsupportedOperationException("Club Name has not be filled, problem");
        }
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

}
