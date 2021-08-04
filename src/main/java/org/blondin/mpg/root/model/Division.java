package org.blondin.mpg.root.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Division {

    private Map<String, String> usersTeams;

    public Map<String, String> getUsersTeams() {
        return usersTeams;
    }

    public String getTeam(String userId) {
        if (!getUsersTeams().containsKey(userId)) {
            throw new UnsupportedOperationException(String.format("No team for user: %s", userId));
        }
        return getUsersTeams().get(userId);
    }
}
