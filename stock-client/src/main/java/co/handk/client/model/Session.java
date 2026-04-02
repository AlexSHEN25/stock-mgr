package co.handk.client.model;

public class Session {
    private static String token;
    private static Long userId;
    private static String username;

    public static void set(String t, Long uid, String name) {
        token = t;
        userId = uid;
        username = name;
    }

    public static String getToken() {
        return token;
    }

    public static Long getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static void clear() {
        token = null;
        userId = null;
        username = null;
    }
}
