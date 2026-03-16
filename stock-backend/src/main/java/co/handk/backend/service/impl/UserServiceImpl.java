package co.handk.backend.service.impl;

import co.handk.backend.entity.User;
import co.handk.backend.entity.UserToken;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.mapper.UserTokenMapper;
import co.handk.backend.service.UserService;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.util.JwtUtil;
import co.handk.common.util.PasswordUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static co.handk.common.constant.CommonConstant.LOGIN_EXPIRE_DAYS;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public LoginVO login(LoginDTO dto, String ip) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String password = PasswordUtil.encrypt(dto.getPassword(), user.getSalt());

        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        // 生成JWT
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        String tokenKey = "login:token:" + token;
        String userKey = "login:user:" + user.getId();

        // 单点登录：删除旧token
        String oldToken = redisTemplate.opsForValue().get(userKey);
        if(oldToken != null){
            redisTemplate.delete("login:token:" + oldToken);
        }
        // 存Redis
        redisTemplate.opsForValue().set(tokenKey, user.getId().toString(), 7, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(userKey, token, LOGIN_EXPIRE_DAYS, TimeUnit.DAYS);
        // 写登录记录
        UserToken record = new UserToken();
        record.setUserId(user.getId());
        record.setToken(token);
        record.setLoginIp(ip);
        record.setLoginTime(LocalDateTime.now());
        record.setExpireTime(LocalDateTime.now().plusDays(7));
        record.setStatus(1);
        userTokenMapper.insert(record);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        return vo;
    }

    @Override
    public Boolean logout(String token) {
        String tokenKey = "login:token:" + token;
        return redisTemplate.delete(tokenKey);
    }
}