package co.handk.backend.service.impl;

import co.handk.backend.util.EnumFieldMapper;

import co.handk.backend.entity.Permission;
import co.handk.common.model.dto.create.CreatePermissionDTO;
import co.handk.common.model.dto.update.UpdatePermissionDTO;
import co.handk.common.model.vo.PermissionVO;
import co.handk.backend.mapper.PermissionMapper;
import co.handk.backend.service.PermissionService;
import co.handk.common.model.dto.query.PermissionQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public Boolean create(CreatePermissionDTO dto) {
        Permission entity = new Permission();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public PermissionVO get(Long id) {
        Permission entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdatePermissionDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        Permission entity = new Permission();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(Permission::getId, id).set(Permission::getDeleted, 1).update();
    }

    @Override
    public PageResult<PermissionVO> pageQuery(PermissionQueryDTO query) {
        Page<Permission> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getDeleted, 0).orderByDesc(Permission::getUpdateTime);
        Page<Permission> resultPage =     permissionMapper.selectPage(page, wrapper);
        List<PermissionVO> records = resultPage.getRecords().stream().map(entity -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
