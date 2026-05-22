package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.User;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.service.LoginService;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.RedisKey;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.util.PasswordUtil;
import co.handk.common.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final UserMapper userMapper;
    private final StringRedisUtil stringRedisUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        String username = dto.getUsername();
        User user = userMapper.selectByUsername(
                username,
                StatusEnum.NOMAL.getCode(),
                DeleteEnum.UNDELETED.getCode()
        );
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
        }
        String token = TokenUtil.generateToken();
        String tokenKey = RedisKey.LOGIN_TOKEN + token;
        stringRedisUtil.set(tokenKey, userId.toString(), CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
        stringRedisUtil.set(userKey, token, CommonConstant.EXPIRE_TIME, TimeUnit.MINUTES);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(userId);
        vo.setUsername(username);
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
        }
        stringRedisUtil.delete(userKey);
        return LogoutVO.success(userId);
    }
}
