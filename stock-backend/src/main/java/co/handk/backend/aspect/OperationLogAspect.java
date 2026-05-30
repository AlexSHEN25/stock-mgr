package co.handk.backend.aspect;

import co.handk.backend.constant.OperateLogConstant;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.OperateLog;
import co.handk.backend.entity.User;
import co.handk.backend.service.OperateLogService;
import co.handk.backend.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final String UNKNOWN = "UNKNOWN";

    private final OperateLogService operateLogService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object aroundController(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (!shouldLog(request)) {
            return point.proceed();
        }

        long start = System.currentTimeMillis();
        Object result = null;
        Throwable throwable = null;
        try {
            result = point.proceed();
            return result;
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            saveLog(point, request, result, throwable, System.currentTimeMillis() - start);
        }
    }

    private void saveLog(ProceedingJoinPoint point,
                         HttpServletRequest request,
                         Object result,
                         Throwable throwable,
                         long costTime) {
        try {
            OperateLog log = new OperateLog();
            Long userId = UserContext.getUserIdOrDefault();
            User user = userService.getByIdNotDeleted(userId);
            log.setUserId(userId);
            log.setUsername(user == null ? null : user.getUsername());
            log.setModule(resolveModule(point));
            log.setOperation(resolveOperation(request));
            log.setMethod(resolveMethod(point));
            log.setRequestUrl(request == null ? UNKNOWN : request.getRequestURI());
            log.setRequestIp(resolveIp(request));
            log.setRequestParam(truncate(maskSensitive(toJson(filterArgs(point.getArgs())))));
            log.setResponseData(truncate(maskSensitive(toJson(result))));
            log.setStatus(throwable == null ? OperateLogConstant.SUCCESS : OperateLogConstant.FAILED);
            log.setErrorMsg(throwable == null ? null : truncate(maskSensitive(throwable.getMessage())));
            log.setCostTime((int) costTime);
            operateLogService.save(log);
        } catch (Exception e) {
            log.warn("操作ログ書き込み失敗: {}", e.getMessage(), e);
        }
    }

    private boolean shouldLog(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
            return false;
        }
        String uri = request.getRequestURI();
        if (uri != null && (uri.endsWith("/page") || uri.endsWith("/list"))) {
            return false;
        }
        return uri != null
                && !uri.contains("/operateLog")
                && !uri.contains("/login")
                && !uri.contains("/logout");
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String resolveModule(ProceedingJoinPoint point) {
        String className = point.getTarget().getClass().getSimpleName();
        return className.endsWith("Controller")
                ? className.substring(0, className.length() - "Controller".length())
                : className;
    }

    private String resolveOperation(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String method = request.getMethod().toUpperCase();
        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> method;
        };
    }

    private String resolveMethod(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        return signature.getDeclaringTypeName() + "." + signature.getName();
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private List<Object> filterArgs(Object[] args) {
        List<Object> result = new ArrayList<>();
        if (args == null) {
            return result;
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof ServletRequest
                    || arg instanceof ServletResponse
                    || arg instanceof BindingResult
                    || arg instanceof MultipartFile) {
                continue;
            }
            result.add(arg);
        }
        return result;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private String maskSensitive(String value) {
        if (value == null) {
            return null;
        }
        return value
                .replaceAll("(?i)\\\"password\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"password\":\"***\"")
                .replaceAll("(?i)\\\"token\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"token\":\"***\"");
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= OperateLogConstant.MAX_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, OperateLogConstant.MAX_TEXT_LENGTH);
    }
}
