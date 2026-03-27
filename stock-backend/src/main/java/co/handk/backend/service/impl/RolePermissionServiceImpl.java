package co.handk.backend.service.impl;

import co.handk.backend.entity.RolePermission;
import co.handk.common.model.dto.RolePermissionDTO;
import co.handk.backend.mapper.RolePermissionMapper;
import co.handk.backend.service.RolePermissionService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public Boolean create(RolePermissionDTO dto) {
        RolePermission entity = new RolePermission();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public RolePermission get(Long id) {
        RolePermission entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        return entity;
    }

    @Override
    public Boolean update(RolePermissionDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        RolePermission entity = new RolePermission();
        BeanUtils.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<RolePermission> listAll() {
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getDeleted, 0).orderByDesc(RolePermission::getUpdateTime);
        return rolePermissionMapper.selectList(wrapper);
    }

    @Override
    public PageResult<RolePermission> pageQuery(PageQuery query) {
        Page<RolePermission> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getDeleted, 0).orderByDesc(RolePermission::getUpdateTime);
        Page<RolePermission> resultPage = rolePermissionMapper.selectPage(page, wrapper);
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), resultPage.getRecords());
    }
}
