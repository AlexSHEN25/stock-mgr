package co.handk.client.util;

import org.json.JSONObject;

public final class JsonPayloadHelper {

    private JsonPayloadHelper() {
    }

    public static boolean isBlank(Object value) {
        return value == null
                || value == JSONObject.NULL
                || String.valueOf(value).trim().isEmpty()
                || "null".equalsIgnoreCase(String.valueOf(value).trim());
    }

    public static void putIfPresent(JSONObject target, JSONObject source, String key) {
        if (target == null || source == null || key == null || key.isBlank()) {
            return;
        }
        Object value = source.opt(key);
        if (!isBlank(value)) {
            target.put(key, value);
        }
    }

    public static void putIntegerIfPresent(JSONObject target, JSONObject source, String key) {
        Number number = coerceNumber(source, key);
        if (number != null) {
            target.put(key, number.intValue());
        }
    }

    public static void putLongIfPresent(JSONObject target, JSONObject source, String key) {
        Number number = coerceNumber(source, key);
        if (number != null) {
            target.put(key, number.longValue());
        }
    }

    public static Number coerceNumber(JSONObject source, String key) {
        if (source == null || key == null || key.isBlank() || !source.has(key)) {
            return null;
        }
        Object value = source.opt(key);
        if (value == null || value == JSONObject.NULL || value instanceof Boolean) {
            return null;
        }
        if (value instanceof Number number) {
            return number;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            if (text.contains(".")) {
                return Double.parseDouble(text);
            }
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
