package org.blondin.mpg.stats.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.blondin.mpg.root.model.StatsDayOrPositionPlayer;
import org.blondin.mpg.stats.model.Position;
import org.blondin.mpg.stats.model.StatsDay;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class StatsDayOrPositionPlayerDeserializer extends StdDeserializer<StatsDayOrPositionPlayer> {

    private static final long serialVersionUID = -3878716532565320657L;

    public StatsDayOrPositionPlayerDeserializer() {
        this(StatsDayOrPositionPlayer.class);
    }

    protected StatsDayOrPositionPlayerDeserializer(Class<?> vc) {
        super(vc);
    }

    @SuppressWarnings("unchecked")
    @Override
    public StatsDayOrPositionPlayer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Object o = ctxt.readValue(jp, Object.class);
        StatsDayOrPositionPlayer sop = new StatsDayOrPositionPlayer();
        if (o instanceof String) {
            sop.setPosition(Position.getNameByValue((String) o));
        }
        if (o instanceof ArrayList<?>) {
            Map<Integer, StatsDay> statsDay = new HashMap<>();
            for (ArrayList<?> e : (ArrayList<ArrayList<?>>) o) {
                Integer day = (Integer) e.get(0);
                Map<String, Object> values = (Map<String, Object>) e.get(1);
                double average = 0;
                if (values.containsKey("n")) {
                    average = Double.valueOf(values.get("n").toString());
                }
                int goals = 0;
                if (values.containsKey("g")) {
                    goals = (Integer) values.get("g");
                }
                statsDay.put(day, new StatsDay(average, goals));
            }
            sop.setStatsDay(statsDay);
        }
        return sop;
    }

}
