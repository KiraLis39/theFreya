package game.freya.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonUtils {
    private JsonUtils() {
    }

    public static String getJsonValueOrNull(JsonNode node, String value) {
        return value != null && node.has(value) && !node.get(value).isNull()
                && !node.get(value).asText().isEmpty() ? node.get(value).asText() : null;
    }
}
