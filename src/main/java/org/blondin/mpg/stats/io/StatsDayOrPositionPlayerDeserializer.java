package org.blondin.mpg.stats.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
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
        switch (o) {
        case String objString -> sop.setPosition(Position.getNameByValue(objString));
        case ArrayList<?> objArrayList -> {
            Map<Integer, StatsDay> statsDay = new HashMap<>();
            if (!objArrayList.isEmpty() && objArrayList.get(0) instanceof HashMap<?, ?>) {
                // API v2 format
                for (HashMap<?, ?> e : (ArrayList<HashMap<?, ?>>) o) {
                    statsDay.put(Integer.parseInt(e.get("D").toString()),
                            new StatsDay(Double.valueOf(ObjectUtils.getIfNull(e.get("n"), 0).toString()), Integer.valueOf(ObjectUtils.getIfNull(e.get("g"), 0).toString())));
                }
            } else {
                for (ArrayList<?> e : (ArrayList<ArrayList<?>>) o) {
                    Integer day = (Integer) e.get(0);
                    Map<String, Object> values = (Map<String, Object>) e.get(1);
                    statsDay.put(day, new StatsDay(Double.valueOf(ObjectUtils.getIfNull(values.get("n"), 0).toString()), Integer.valueOf(ObjectUtils.getIfNull(values.get("g"), 0).toString())));
                }
            }
            sop.setStatsDay(statsDay);
        }
        default -> new UnsupportedOperationException("Object is not a 'Position' or 'StatsDay' array");
        }
        return sop;
    }

}
