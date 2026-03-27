package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.User;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.service.UserService;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.RedisKey;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.util.PasswordUtil;
import co.handk.common.util.TokenUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisUtil stringRedisUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        String username = dto.getUsername();
        String rawPassword = dto.getPassword();
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String encryptPwd = PasswordUtil.encrypt(rawPassword, user.getSalt());
        if (!user.getPassword().equals(encryptPwd)) {
            throw new RuntimeException("用户名或密码错误");
        }
        Long userId = user.getId();
        String userKey = RedisKey.LOGIN_USER + userId;
        // 单点登录
        String oldToken = stringRedisUtil.get(userKey);
        if (StringUtils.isNotBlank(oldToken)) {
            stringRedisUtil.delete(RedisKey.LOGIN_TOKEN + oldToken);
            stringRedisUtil.delete(userKey);
        }
        // 生成token
        String token = TokenUtil.generateToken();
        String tokenKey = RedisKey.LOGIN_TOKEN + token;
        // 写入Redis
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
        // 幂等设计：没登录也返回成功
        if (Objects.isNull(userId)) {
            return LogoutVO.success(null);
        }
        String userKey = RedisKey.LOGIN_USER + userId;
        // 1. 获取当前 token
        String token = stringRedisUtil.get(userKey);
        // 2. 删除 tokenKey
        if (StringUtils.isNotBlank(token)) {
            String tokenKey = RedisKey.LOGIN_TOKEN + token;
            stringRedisUtil.delete(tokenKey);
        }
        // 3. 删除 userKey
        stringRedisUtil.delete(userKey);
        return LogoutVO.success(userId);
    }

    @Override
    public PageResult<User> pageQuery(PageQuery query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0).orderByDesc(User::getUpdateTime);
        Page<User> resultPage = userMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
