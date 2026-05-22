package co.handk.backend.interceptor;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.SecurityConstant;
import co.handk.backend.context.UserContext;
import co.handk.backend.entity.Permission;
import co.handk.backend.exception.AccessDeniedException;
import co.handk.backend.service.PermissionQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    private static final String EMPTY = "";
    private static final String API_STOCK_ORDER_PREFIX = "/api/stockOrder";
    private static final String API_STOCK_ORDER_ITEM_PREFIX = "/api/stockOrderItem";
    private static final String API_REQUEST_FORM_PREFIX = "/api/requestForm";
    private static final String API_REQUEST_ITEM_PREFIX = "/api/requestItem";
    private static final String API_CUSTOMER_PREFIX = "/api/customer";

    private final PermissionQueryService permissionQueryService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return true;
        }
        if (permissionQueryService.isSuperAdmin(userId)) {
            return true;
        }

        String uri = normalizePath(request.getRequestURI());
        if (isNonAdminWriteAllowedByWhitelist(uri, request.getMethod())) {
            return true;
        }
        String requiredCode = resolveRequiredPermission(uri, request.getMethod());
        if (requiredCode == null) {
            throw new AccessDeniedException(MessageKeyConstant.ERROR_NO_PERMISSION, SecurityConstant.NO_PERMISSION_MESSAGE);
        }

        Set<String> codes = permissionQueryService.getPermissionCodes(userId);
        if (!codes.contains(requiredCode)) {
            throw new AccessDeniedException(MessageKeyConstant.ERROR_NO_PERMISSION, SecurityConstant.NO_PERMISSION_MESSAGE);
        }
        return true;
    }

    private String resolveRequiredPermission(String uri, String method) {
        boolean read = HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method);
        List<Permission> dataPermissions = permissionQueryService.getEnabledDataPermissions();
        String altUri = uri;
        if (uri != null && uri.startsWith(SecurityConstant.API_PREFIX)) {
            altUri = uri.substring(SecurityConstant.API_PREFIX_KEEP_LEADING_SLASH_INDEX);
        }

        for (Permission permission : dataPermissions) {
            String path = permission.getPath();
            String code = permission.getCode();
            if (path == null || path.isBlank() || code == null || code.isBlank()) {
                continue;
            }
            if (!pathMatcher.match(path, uri) && !pathMatcher.match(path, altUri)) {
                continue;
            }
            if (read && code.endsWith(SecurityConstant.PERMISSION_SUFFIX_READ)) {
                return code;
            }
            if (!read && code.endsWith(SecurityConstant.PERMISSION_SUFFIX_WRITE)) {
                return code;
            }
        }
        return null;
    }

    private boolean isNonAdminWriteAllowedByWhitelist(String uri, String method) {
        if (uri == null) {
            return false;
        }
        String normalizedMethod = method == null ? EMPTY : method.toUpperCase(Locale.ROOT);
        boolean isWrite = !HttpMethod.GET.matches(normalizedMethod)
                && !HttpMethod.HEAD.matches(normalizedMethod)
                && !HttpMethod.OPTIONS.matches(normalizedMethod);
        if (!isWrite) {
            return false;
        }
        return startsWithPath(uri, API_STOCK_ORDER_PREFIX)
                || startsWithPath(uri, API_STOCK_ORDER_ITEM_PREFIX)
                || startsWithPath(uri, API_REQUEST_FORM_PREFIX)
                || startsWithPath(uri, API_REQUEST_ITEM_PREFIX)
                || startsWithPath(uri, API_CUSTOMER_PREFIX);
    }

    private boolean startsWithPath(String uri, String prefix) {
        return uri.equals(prefix) || uri.startsWith(prefix + "/");
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return EMPTY;
        }
        return path;
    }
}
