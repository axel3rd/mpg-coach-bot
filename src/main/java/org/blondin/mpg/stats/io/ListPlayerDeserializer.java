package org.blondin.mpg.stats.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.blondin.mpg.stats.model.Player;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ListPlayerDeserializer extends StdDeserializer<List<Player>> {

    private static final long serialVersionUID = -7012195953299512960L;

    public ListPlayerDeserializer() {
        this(List.class);
    }

    protected ListPlayerDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<Player> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);
        if (root.get(0) instanceof ArrayNode) {
            List<Player> list = new ArrayList<>();
            for (JsonNode node : root) {
                list.add(mapper.readValue(node.get(1).traverse(), Player.class));
            }
            return list;
        }
        return Arrays.asList(mapper.readValue(root.traverse(), Player[].class));
    }

}
