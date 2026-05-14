package co.handk.backend.interceptor;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.context.UserContext;
import co.handk.backend.exception.LoginException;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.RedisKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (StringUtils.isBlank(auth) || !auth.startsWith("Bearer ")) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_REQUIRED, "login required");
        }

        String token = auth.substring(7).trim();
        String tokenKey = RedisKey.LOGIN_TOKEN + token;
        String userId = redisUtil.get(tokenKey);
        if (StringUtils.isBlank(userId)) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_INVALID, "invalid login session");
        }

        String userKey = RedisKey.LOGIN_USER + userId;
        String latestToken = redisUtil.get(userKey);
        if (!token.equals(latestToken)) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_REPLACED, "session replaced by another login");
        }

        Long ttl = redisUtil.getExpire(tokenKey, TimeUnit.MINUTES);
        if (Objects.nonNull(ttl) && ttl < CommonConstant.UPDATE_EXPIRE_TIME) {
            redisUtil.expire(tokenKey, CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
            redisUtil.expire(userKey, CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
        }

        try {
            UserContext.setUserId(Long.valueOf(userId));
        } catch (NumberFormatException ex) {
            throw new LoginException(MessageKeyConstant.ERROR_LOGIN_INVALID, "invalid login session");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
