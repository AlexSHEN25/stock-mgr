package co.handk.client.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Session {
    private static final ZoneId SESSION_ZONE = ZoneId.of("Asia/Tokyo");
    private static final DateTimeFormatter SPACE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String token;
    private static Long userId;
    private static String username;
    private static long expireAtMillis;
    private static Set<String> permissionCodes = Collections.emptySet();

    public static void set(String t, Long uid, String name) {
        set(t, uid, name, null);
    }

    public static void set(String t, Long uid, String name, String expireTime) {
        token = t;
        userId = uid;
        username = name;
        updateExpireTime(expireTime);
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

    public static Set<String> getPermissionCodes() {
        return permissionCodes;
    }

    public static void setPermissionCodes(Set<String> codes) {
        permissionCodes = codes == null ? Collections.emptySet() : new HashSet<>(codes);
    }

    public static boolean hasPermission(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return permissionCodes.contains("DATA_ALL_WRITE") || permissionCodes.contains(code);
    }

    public static boolean isNormalUser() {
        return permissionCodes.contains("ROLE_NORMAL_USER") && !permissionCodes.contains("ROLE_SUPER_ADMIN");
    }

    public static long getExpireAtMillis() {
        return expireAtMillis;
    }

    public static void updateExpireTime(String expireTime) {
        expireAtMillis = parseExpireTime(expireTime);
    }

    public static boolean shouldRefresh(long thresholdMillis) {
        return token != null
                && !token.isBlank()
                && expireAtMillis > 0
                && expireAtMillis - System.currentTimeMillis() <= thresholdMillis;
    }

    public static void clear() {
        token = null;
        userId = null;
        username = null;
        expireAtMillis = 0L;
        permissionCodes = Collections.emptySet();
    }

    private static long parseExpireTime(String expireTime) {
        if (expireTime == null || expireTime.isBlank()) {
            return 0L;
        }
        String value = expireTime.trim();
        try {
            return LocalDateTime.parse(value).atZone(SESSION_ZONE).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value, SPACE_DATE_TIME).atZone(SESSION_ZONE).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        return 0L;
    }
}
