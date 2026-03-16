package co.handk.backend.interceptor;

import co.handk.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

import static co.handk.common.constant.CommonConstant.LOGIN_EXPIRE_DAYS;

public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String auth = request.getHeader("Authorization");

        if (auth == null) {
            throw new RuntimeException("未登录");
        }

        String token = auth.replace("Bearer ", "");

        Claims claims = JwtUtil.parseToken(token);

        Integer userId = claims.get("userId", Integer.class);

        // Redis校验
        String redisKey = "login:token:" + token;

        String redisUserId = redisTemplate.opsForValue().get(redisKey);

        if (redisUserId == null) {
            throw new RuntimeException("登录已失效");
        }
        // 自动续期
        redisTemplate.expire(redisKey, LOGIN_EXPIRE_DAYS, TimeUnit.DAYS);
        return true;
    }
}