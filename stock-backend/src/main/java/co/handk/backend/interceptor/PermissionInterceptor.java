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
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

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
        String requiredCode = resolveRequiredPermission(uri, request.getMethod());
        if (requiredCode == null) {
            throw new AccessDeniedException(MessageKeyConstant.ERROR_NO_PERMISSION, "no permission");
        }

        Set<String> codes = permissionQueryService.getPermissionCodes(userId);
        if (!codes.contains(requiredCode)) {
            throw new AccessDeniedException(MessageKeyConstant.ERROR_NO_PERMISSION, "no permission");
        }
        return true;
    }

    private String resolveRequiredPermission(String uri, String method) {
        boolean read = HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method);
        List<Permission> dataPermissions = permissionQueryService.getEnabledDataPermissions();

        for (Permission permission : dataPermissions) {
            String path = permission.getPath();
            String code = permission.getCode();
            if (path == null || path.isBlank() || code == null || code.isBlank()) {
                continue;
            }
            if (!pathMatcher.match(path, uri)) {
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

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        if (path.startsWith(SecurityConstant.API_PREFIX)) {
            return path.substring(SecurityConstant.API_PREFIX.length() - 1);
        }
        return path;
    }
}
