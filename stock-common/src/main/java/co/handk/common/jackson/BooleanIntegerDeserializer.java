package co.handk.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class BooleanIntegerDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_TRUE) {
            return 1;
        }
        if (token == JsonToken.VALUE_FALSE) {
            return 0;
        }
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return parser.getIntValue();
        }
        if (token == JsonToken.VALUE_STRING) {
            String text = parser.getText() == null ? "" : parser.getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            if ("true".equalsIgnoreCase(text)) {
                return 1;
            }
            if ("false".equalsIgnoreCase(text)) {
                return 0;
            }
            return Integer.valueOf(text);
        }
        return (Integer) context.handleUnexpectedToken(Integer.class, parser);
    }
}
