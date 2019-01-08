package org.blondin.mpg.stats.model;

import java.util.Date;
import java.util.List;

import org.blondin.mpg.root.exception.PlayerNotFoundException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Championship (mpgstats)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Championship {

    @JsonProperty("p")
    private List<Player> players;

    @JsonProperty("mxD")
    private int day;

    @JsonProperty("bD")
    private Date date;

    @JsonProperty("mL")
    private Infos infos;

    /**
     * Current day of season
     * 
     * @return The day
     */
    public int getDay() {
        return day;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getPlayer(String name) {
        for (Player p : getPlayers()) {
            if (p.getName().equals(name) || p.getLastName().equals(name)) {
                return p;
            }
        }
        throw new PlayerNotFoundException(String.format("Player can't be found in stats: %s", name));
    }

    public Infos getInfos() {
        return infos;
    }

    public Date getDate() {
        return date;
    }
}
