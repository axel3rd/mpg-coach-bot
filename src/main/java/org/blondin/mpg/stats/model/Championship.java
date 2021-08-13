package org.blondin.mpg.stats.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.blondin.mpg.root.exception.PlayerNotFoundException;
import org.blondin.mpg.stats.io.ListPlayerDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Championship (mpgstats)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Championship {

    @JsonDeserialize(using = ListPlayerDeserializer.class)
    @JsonProperty("p")
    private List<Player> players = new ArrayList<>();

    @JsonProperty("bD")
    private Date date;

    @JsonProperty("mL")
    private Infos infos;

    private boolean maxDaySetOnPlayer;

    public List<Player> getPlayers() {
        synchronized (this) {
            if (!maxDaySetOnPlayer) {
                // If last season statistics does not exist (Dominos L2 in 2019 use case, first one in MPG), current annual max day is used
                final int previousMaxSeasonDay = getInfos().getLastStats() == null ? getInfos().getAnnualStats().getMaxDay()
                        : getInfos().getLastStats().getMaxDay();
                final int currentSeasonDay = getInfos().getAnnualStats().getCurrentDay().getDayReached();
                players.forEach(p -> p.getStats().setCurrentSeasonDay(currentSeasonDay));
                players.forEach(p -> p.getStats().setPreviousMaxSeasonDay(previousMaxSeasonDay));
                maxDaySetOnPlayer = true;
            }
        }
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
