package org.blondin.mpg.stats.model;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.root.model.StatsDayOrPositionPlayer;
import org.blondin.mpg.stats.io.StatsDayOrPositionPlayerDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Player
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    @JsonProperty("f")
    private String firstName;
    @JsonProperty("n")
    private String lastName;
    @JsonDeserialize(using = StatsDayOrPositionPlayerDeserializer.class)
    @JsonProperty("p")
    private StatsDayOrPositionPlayer statsDayOrPosition;
    @JsonProperty("fp")
    private Position position;
    @JsonProperty("s")
    private Stats stats;
    @JsonProperty("r")
    private int price;
    @JsonProperty("a")
    private Auction auction;
    @JsonProperty("la")
    private Auction auctionLong;

    private double efficiency;

    public String getFirstName() {
        return StringUtils.defaultIfBlank(firstName, "");
    }

    public String getLastName() {
        return StringUtils.defaultIfBlank(lastName, "");
    }

    public String getName() {
        if (StringUtils.isBlank(getFirstName()) && getLastName().contains(" ")) {
            int index = getLastName().indexOf(' ');
            return getLastName().substring(index + 1) + " " + getLastName().substring(0, index);
        }
        return (getLastName() + " " + getFirstName()).trim();
    }

    public int getPrice() {
        return price;
    }

    public Auction getAuction() {
        return auction;
    }

    public Auction getAuctionLong() {
        return auctionLong;
    }

    public Stats getStats() {
        if (stats == null) {
            stats = new Stats();
        }
        if (stats.getStatsDay() == null && statsDayOrPosition != null && statsDayOrPosition.getStatsDay() != null) {
            stats.setStatsDay(statsDayOrPosition.getStatsDay());
        }
        return stats;
    }

    public Position getPosition() {
        if (position != null) {
            return position;
        }
        if (statsDayOrPosition.getPosition() != null) {
            return statsDayOrPosition.getPosition();
        }
        throw new UnsupportedOperationException("No position found");
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

}
