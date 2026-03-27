package co.handk.backend.service.impl;

import co.handk.backend.entity.UserRole;
import co.handk.backend.mapper.UserRoleMapper;
import co.handk.backend.service.UserRoleService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    private final UserRoleMapper userRoleMapper;

    @Override
    public Boolean create(UserRole entity) {
        if (entity == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public UserRole get(Long id) {
        UserRole entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(UserRole entity) {
        if (entity == null || Objects.isNull(entity.getId())) {
            throw new RuntimeException("ID不能为空");
        }
        if (this.getById(entity.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (Objects.isNull(id)) {
            throw new RuntimeException("ID不能为空");
        }
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<UserRole> listAll() {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getDeleted, 0).orderByDesc(UserRole::getUpdateTime);
        return userRoleMapper.selectList(wrapper);
    }

    @Override
    public PageResult<UserRole> pageQuery(PageQuery query) {
        Page<UserRole> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getDeleted, 0).orderByDesc(UserRole::getUpdateTime);
        Page<UserRole> resultPage = userRoleMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
