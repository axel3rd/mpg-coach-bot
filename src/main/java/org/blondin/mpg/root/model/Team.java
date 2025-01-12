package org.blondin.mpg.root.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {

    private String name;
    private int budget;
    private Map<String, Player> squad;
    private List<Player> bids;
    private Map<String, Object> bonuses;

    /**
     * Since January 2025 Bonuses become not only a list of "key:int".<br/>
     * But a new key with (wrong?) sum was introduced, with linked "key:int" list.<br/>
     * So hack was found to keep previous Map<String, Integer> getBonuses() method sign.<br/>
     * "bonuses": {<br/>
     * "10": {<br/>
     * "blockTacticalSubs": 1,<br/>
     * "boostAllPlayers": 1,<br/>
     * "boostOnePlayer": 3,<br/>
     * "fourStrikers": 1,<br/>
     * "mirror": 1,<br/>
     * "nerfGoalkeeper": 2,<br/>
     * "removeGoal": 1,<br/>
     * "removeRandomPlayer": 1<br/>
     * },<br/>
     * "blockTacticalSubs": 1,<br/>
     * "boostAllPlayers": 1,<br/>
     * "boostOnePlayer": 3,<br/>
     * "fourStrikers": 1,<br/>
     * "mirror": 1,<br/>
     * "nerfGoalkeeper": 2,<br/>
     * "removeGoal": 1,<br/>
     * "removeRandomPlayer": 1<br/>
     * },
     * 
     * @return Map of Bonuses
     */
    public Map<String, Integer> getBonuses() {
        return bonuses.entrySet().stream().filter(entry -> entry.getValue() instanceof Integer).collect(Collectors.toMap(Map.Entry::getKey, entry -> (Integer) entry.getValue()));
    }

    public int getBonusesNumber() {
        return getBonuses().values().stream().reduce(0, Integer::sum);
    }

    public String getName() {
        return name;
    }

    public int getBudget() {
        return budget;
    }

    public Map<String, Player> getSquad() {
        return squad;
    }

    public List<Player> getBids() {
        return bids;
    }
}
