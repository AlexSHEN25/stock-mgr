package co.handk.backend.interceptor;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.constant.SecurityConstant;
import co.handk.backend.context.UserContext;
import co.handk.backend.exception.LoginException;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.RedisKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final StringRedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader(SecurityConstant.AUTHORIZATION_HEADER);
        if (StringUtils.isBlank(auth) || !auth.startsWith(SecurityConstant.BEARER_PREFIX)) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_REQUIRED, "ログインが必要です");
        }

        String token = auth.substring(SecurityConstant.BEARER_PREFIX.length()).trim();
        String tokenKey = RedisKey.LOGIN_TOKEN + token;
        String userId = redisUtil.get(tokenKey);
        if (StringUtils.isBlank(userId)) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_INVALID, "ログインセッションが無効です");
        }

        String userKey = RedisKey.LOGIN_USER + userId;
        String latestToken = redisUtil.get(userKey);
        if (!token.equals(latestToken)) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_REPLACED, "他のログインによりセッションが無効化されました");
        }

        Long ttl = redisUtil.getExpire(tokenKey, TimeUnit.MINUTES);
        if (Objects.nonNull(ttl) && ttl < CommonConstant.UPDATE_EXPIRE_TIME) {
            redisUtil.expire(tokenKey, CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
            redisUtil.expire(userKey, CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
        }

        try {
            UserContext.setUserId(Long.valueOf(userId));
        } catch (NumberFormatException ex) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_INVALID, "ログインセッションが無効です");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}