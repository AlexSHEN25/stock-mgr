package co.handk.backend.constant;

public final class SecurityConstant {

    private SecurityConstant() {
    }

    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String PERMISSION_SUFFIX_READ = "_READ";
    public static final String PERMISSION_SUFFIX_WRITE = "_WRITE";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String API_PREFIX = "/api/";
    public static final int API_PREFIX_KEEP_LEADING_SLASH_INDEX = API_PREFIX.length() - 1;
    public static final String NO_PERMISSION_MESSAGE = "無権限アクセス";
}
