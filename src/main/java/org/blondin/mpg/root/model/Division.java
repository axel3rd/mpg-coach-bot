package org.blondin.mpg.root.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Division {

    private Map<String, String> usersTeams;

    @JsonProperty("liveState")
    private DivisionLiveStat liveState;

    public Map<String, String> getUsersTeams() {
        return usersTeams;
    }

    public String getTeam(String userId) {
        if (!getUsersTeams().containsKey(userId)) {
            throw new UnsupportedOperationException(String.format("No team for user: %s", userId));
        }
        return getUsersTeams().get(userId);
    }

    public int getGameTotal() {
        return liveState.getTotalGameWeeks();
    }

    public int getGameCurrent() {
        return liveState.getCurrentGameWeek();
    }

    public int getGameRemaining() {
        return getGameTotal() - getGameCurrent() + 1;
    }
}
