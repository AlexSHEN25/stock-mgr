package co.handk.backend.constant;

import java.util.List;

public final class SecurityConstant {

    private SecurityConstant() {
    }

    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_NORMAL_USER = "ROLE_NORMAL_USER";
    public static final String DATA_ALL_WRITE = "DATA_ALL_WRITE";
    public static final String PERMISSION_SUFFIX_READ = "_READ";
    public static final String PERMISSION_SUFFIX_WRITE = "_WRITE";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String API_PREFIX = "/api/";
    public static final int API_PREFIX_KEEP_LEADING_SLASH_INDEX = API_PREFIX.length() - 1;
    public static final String NO_PERMISSION_MESSAGE = "権限がありません";
    public static final List<String> NORMAL_USER_WRITE_API_PREFIXES = List.of(
            "/api/stockOrder",
            "/api/stockOrderItem",
            "/api/requestForm",
            "/api/requestItem",
            "/api/customer",
            "/api/customerLevel"
    );

    public static boolean isNormalUserWriteApiPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return NORMAL_USER_WRITE_API_PREFIXES.stream()
                .anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
    }
}
