
package org.blondin.mpg.root.model;

import java.util.Map;

import org.blondin.mpg.stats.model.Position;
import org.blondin.mpg.stats.model.StatsDay;

public class StatsDayOrPositionPlayer {

    private Map<Integer, StatsDay> statsDay;
    private Position position;

    public Map<Integer, StatsDay> getStatsDay() {
        return statsDay;
    }

    public void setStatsDay(Map<Integer, StatsDay> statsDay) {
        this.statsDay = statsDay;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
