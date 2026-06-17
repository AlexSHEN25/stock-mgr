package co.handk.backend.service.impl;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.User;
import co.handk.backend.entity.UserToken;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.mapper.UserTokenMapper;
import co.handk.backend.service.LoginService;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.RedisKey;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.util.PasswordUtil;
import co.handk.common.util.TokenUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final UserMapper userMapper;
    private final UserTokenMapper userTokenMapper;
    private final StringRedisUtil stringRedisUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        String username = dto.getUsername();
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("username", username)
                .eq("status", StatusEnum.NOMAL.getCode())
                .last("LIMIT 1"));
        if (Objects.isNull(user)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "ユーザーが見つかりません"
            );
        }
        String rawPassword = dto.getPassword();
        String encryptPwd = PasswordUtil.encrypt(rawPassword, user.getSalt());
        if (!user.getPassword().equals(encryptPwd)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "ユーザー名またはパスワードが正しくありません"
            );
        }
        Long userId = user.getId();
        String userKey = RedisKey.LOGIN_USER + userId;
        String oldToken = stringRedisUtil.get(userKey);
        if (StringUtils.isNotBlank(oldToken)) {
            stringRedisUtil.delete(RedisKey.LOGIN_TOKEN + oldToken);
            stringRedisUtil.delete(userKey);
            markTokenInvalidByToken(oldToken);
        }
        String token = TokenUtil.generateToken();
        String tokenKey = RedisKey.LOGIN_TOKEN + token;
        stringRedisUtil.set(tokenKey, userId.toString(), CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
        stringRedisUtil.set(userKey, token, CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
        persistUserToken(userId, token);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setExpireTime(expireAt());
        return vo;
    }

    @Override
    public LoginVO refreshToken() {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "login session is required"
            );
        }
        String userKey = RedisKey.LOGIN_USER + userId;
        String token = stringRedisUtil.get(userKey);
        if (StringUtils.isBlank(token)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "login session is invalid"
            );
        }

        String tokenKey = RedisKey.LOGIN_TOKEN + token;
        stringRedisUtil.expire(tokenKey, CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
        stringRedisUtil.expire(userKey, CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
        LocalDateTime expireAt = expireAt();
        updateTokenExpireTime(token, expireAt);

        User user = userMapper.selectById(userId);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(userId);
        vo.setUsername(user == null ? null : user.getUsername());
        vo.setExpireTime(expireAt);
        return vo;
    }

    @Override
    public LogoutVO logout() {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            return LogoutVO.success(null);
        }
        String userKey = RedisKey.LOGIN_USER + userId;
        String token = stringRedisUtil.get(userKey);
        if (StringUtils.isNotBlank(token)) {
            String tokenKey = RedisKey.LOGIN_TOKEN + token;
            stringRedisUtil.delete(tokenKey);
            markTokenInvalidByToken(token);
        }
        stringRedisUtil.delete(userKey);
        markTokenInvalidByUserId(userId);
        return LogoutVO.success(userId);
    }

    private void persistUserToken(Long userId, String token) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = expireAt(now);
        String loginIp = resolveClientIp();

        UserToken existed = userTokenMapper.selectOne(
                new QueryWrapper<UserToken>()
                        .eq("user_id", userId)
                        .last("LIMIT 1")
        );

        if (existed == null) {
            UserToken entity = new UserToken();
            entity.setUserId(userId);
            entity.setToken(token);
            entity.setLoginTime(now);
            entity.setExpireTime(expireAt);
            entity.setLoginIp(loginIp);
            entity.setStatus(StatusEnum.NOMAL.getCode());
            userTokenMapper.insert(entity);
            return;
        }

        userTokenMapper.update(
                null,
                new UpdateWrapper<UserToken>()
                        .eq("id", existed.getId())
                        .set("token", token)
                        .set("login_time", now)
                        .set("expire_time", expireAt)
                        .set("login_ip", loginIp)
                        .set("status", StatusEnum.NOMAL.getCode())
        );
    }

    private void markTokenInvalidByToken(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        userTokenMapper.update(
                null,
                new UpdateWrapper<UserToken>()
                        .eq("token", token)
                        .set("status", StatusEnum.FOBBIDEN.getCode())
        );
    }

    private void updateTokenExpireTime(String token, LocalDateTime expireAt) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        userTokenMapper.update(
                null,
                new UpdateWrapper<UserToken>()
                        .eq("token", token)
                        .set("expire_time", expireAt)
                        .set("status", StatusEnum.NOMAL.getCode())
        );
    }

    private LocalDateTime expireAt() {
        return expireAt(LocalDateTime.now());
    }

    private LocalDateTime expireAt(LocalDateTime baseTime) {
        return baseTime.plusMinutes(CommonConstant.EXPIRE_TIME);
    }

    private void markTokenInvalidByUserId(Long userId) {
        if (userId == null) {
            return;
        }
        userTokenMapper.update(
                null,
                new UpdateWrapper<UserToken>()
                        .eq("user_id", userId)
                        .set("status", StatusEnum.FOBBIDEN.getCode())
        );
    }

    private String resolveClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
