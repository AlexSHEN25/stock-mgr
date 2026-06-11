package co.handk.backend.interceptor;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.SecurityConstant;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.Permission;
import co.handk.backend.exception.AccessDeniedException;
import co.handk.backend.service.PermissionQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionInterceptor implements HandlerInterceptor {
    private static final String EMPTY = "";

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
        if (isAllowedSelfContextRead(uri, request.getMethod())) {
            return true;
        }
        String requiredCode = resolveRequiredPermission(uri, request.getMethod());
        if (requiredCode == null) {
            if (!isReadRequest(uri, request.getMethod()) && isProtectedApi(uri) && !isAllowedAccountWrite(uri)) {
                log.warn("Permission denied: userId={}, method={}, uri={}, reason=unconfigured-write",
                        userId, request.getMethod(), uri);
                throw new AccessDeniedException(MessageKeyConstant.ERROR_NO_PERMISSION, SecurityConstant.NO_PERMISSION_MESSAGE);
            }
            return true;
        }

        Set<String> codes = permissionQueryService.getPermissionCodes(userId);
        if (!codes.contains(requiredCode)) {
            log.warn("Permission denied: userId={}, method={}, uri={}, requiredCode={}, userCodes={}",
                    userId, request.getMethod(), uri, requiredCode, codes);
            throw new AccessDeniedException(MessageKeyConstant.ERROR_NO_PERMISSION, SecurityConstant.NO_PERMISSION_MESSAGE);
        }
        return true;
    }

    private String resolveRequiredPermission(String uri, String method) {
        boolean read = isReadRequest(uri, method);
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

    private boolean isReadRequest(String uri, String method) {
        String normalizedMethod = method == null ? EMPTY : method.toUpperCase(Locale.ROOT);
        if (HttpMethod.GET.matches(normalizedMethod) || HttpMethod.HEAD.matches(normalizedMethod)) {
            return true;
        }
        String path = uri == null ? EMPTY : uri;
        if (path.startsWith(SecurityConstant.API_PREFIX)) {
            path = path.substring(SecurityConstant.API_PREFIX_KEEP_LEADING_SLASH_INDEX);
        }
        return HttpMethod.POST.matches(normalizedMethod)
                && (path.endsWith("/page") || path.endsWith("/list"));
    }

    private boolean isProtectedApi(String uri) {
        return uri != null && (uri.startsWith(SecurityConstant.API_PREFIX) || uri.startsWith("/"));
    }

    private boolean isAllowedAccountWrite(String uri) {
        if (uri == null) {
            return false;
        }
        String path = uri.startsWith(SecurityConstant.API_PREFIX)
                ? uri.substring(SecurityConstant.API_PREFIX_KEEP_LEADING_SLASH_INDEX)
                : uri;
        return "/user/logout".equals(path)
                || "/user/refresh-token".equals(path)
                || path.matches("/user/\\d+/password");
    }

    private boolean isAllowedSelfContextRead(String uri, String method) {
        if (!HttpMethod.GET.matches(method == null ? EMPTY : method.toUpperCase(Locale.ROOT))) {
            return false;
        }
        if (uri == null) {
            return false;
        }
        String path = uri.startsWith(SecurityConstant.API_PREFIX)
                ? uri.substring(SecurityConstant.API_PREFIX_KEEP_LEADING_SLASH_INDEX)
                : uri;
        return "/user/permission-scope".equals(path)
                || "/user/profile".equals(path)
                || "/user/me".equals(path);
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return EMPTY;
        }
        return path;
    }
}
