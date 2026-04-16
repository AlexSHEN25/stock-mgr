package co.handk.backend.service.impl;

import co.handk.backend.util.PageSortUtil;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.Dept;
import co.handk.backend.entity.User;
import co.handk.backend.mapper.DeptMapper;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.service.UserService;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.RedisKey;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.dto.query.UserQueryDTO;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.dto.create.CreateUserDTO;
import co.handk.common.model.dto.update.UpdateUserDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.model.vo.UserVO;
import co.handk.common.util.PasswordUtil;
import co.handk.common.util.TokenUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisUtil stringRedisUtil;

    @Autowired
    private DeptMapper deptMapper;

    @Override
    public Boolean create(CreateUserDTO dto) {
        validateDeptExists(dto.getDeptId());
        User entity = new User();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        String salt = PasswordUtil.generateSalt();
        entity.setSalt(salt);
        entity.setPassword(PasswordUtil.encrypt(dto.getPassword(), salt));
        return this.save(entity);
    }

    @Override
    public UserVO get(Long id) {
        User entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return toUserVO(entity, buildDeptNameMap(Collections.singletonList(entity)));
    }

    @Override
    public Boolean update(UpdateUserDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        validateDeptExists(dto.getDeptId());
        User entity = new User();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(User::getId, id).set(User::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        String username = dto.getUsername();
        User user = userMapper.selectByUsername(
                username,
                StatusEnum.NOMAL.getCode(),
                DeleteEnum.UNDELETED.getCode()
        );
        if (Objects.isNull(user)) {
            throw new RuntimeException("用户不存在");
        }
        String rawPassword = dto.getPassword();
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
    public PageResult<UserVO> pageQuery(UserQueryDTO query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, DeleteEnum.UNDELETED.getCode())
                .like(StringUtils.isNotBlank(query.getUsername()), User::getUsername, query.getUsername())
                .eq(query.getDeptId() != null, User::getDeptId, query.getDeptId())
                .like(StringUtils.isNotBlank(query.getEmail()), User::getEmail, query.getEmail())
                .like(StringUtils.isNotBlank(query.getPhone()), User::getPhone, query.getPhone())
                .eq(query.getStatus() != null, User::getStatus, (query.getStatus() == null ? null : query.getStatus().getCode()));
        PageSortUtil.applyTimeSort(wrapper, query, User::getCreateTime, User::getUpdateTime);
        Page<User> resultPage = userMapper.selectPage(page, wrapper);
        List<User> users = resultPage.getRecords();
        Map<Long, String> deptNameMap = buildDeptNameMap(users);
        List<UserVO> records = users.stream().map(user -> toUserVO(user, deptNameMap)).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }


    private void validateDeptExists(Long deptId) {
        Dept dept = deptMapper.selectById(deptId);
        if (Objects.isNull(dept)) {
            throw new RuntimeException("部门不存在");
        }
    }

    private Map<Long, String> buildDeptNameMap(List<User> users) {
        List<Long> deptIds = users.stream()
                .map(User::getDeptId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (deptIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return deptMapper.selectBatchIds(deptIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Dept::getId, Dept::getName, (a, b) -> a, HashMap::new));
    }

    private UserVO toUserVO(User user, Map<Long, String> deptNameMap) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
