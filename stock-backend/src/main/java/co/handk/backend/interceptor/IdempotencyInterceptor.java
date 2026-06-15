package co.handk.backend.interceptor;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.RedisKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";
    private static final long TTL_SECONDS = 300L;

    private final StringRedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if (!HttpMethod.POST.matches(method)
                && !HttpMethod.PUT.matches(method)
                && !HttpMethod.DELETE.matches(method)
                && !HttpMethod.PATCH.matches(method)) {
            return true;
        }

        String idempotencyKey = request.getHeader(HEADER_IDEMPOTENCY_KEY);
        if (StringUtils.isBlank(idempotencyKey)) {
            return true;
        }
        if (isStockOutboundRequest(request)) {
            return true;
        }

        Long userId = UserContext.getUserIdOrDefault();
        String requestKey = RedisKey.IDEMPOTENCY_REQUEST
                + userId + ":" + method + ":" + request.getRequestURI() + ":" + idempotencyKey.trim();
        Boolean accepted = redisUtil.setIfAbsent(requestKey, "1", TTL_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(accepted)) {
            throw new BusinessException(
                    MessageKeyConstant.ERROR_RUNTIME,
                    "duplicate request detected, please do not repeat submit"
            );
        }
        return true;
    }

    private boolean isStockOutboundRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/stock") && uri.endsWith("/outbound");
    }
}
