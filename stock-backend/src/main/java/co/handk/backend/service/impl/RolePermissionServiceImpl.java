package co.handk.backend.service.impl;

import co.handk.backend.entity.RolePermission;
import co.handk.backend.mapper.RolePermissionMapper;
import co.handk.backend.service.RolePermissionService;
import co.handk.backend.util.EnumFieldMapper;
import co.handk.backend.util.PageSortUtil;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRolePermissionDTO;
import co.handk.common.model.dto.query.RolePermissionQueryDTO;
import co.handk.common.model.dto.update.UpdateRolePermissionDTO;
import co.handk.common.model.vo.RolePermissionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public Boolean create(CreateRolePermissionDTO dto) {
        RolePermission entity = new RolePermission();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        entity.setId(null);
        return this.save(entity);
    }

    @Override
    public RolePermissionVO get(Long id) {
        RolePermission entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("数据不存在");
        }
        RolePermissionVO vo = new RolePermissionVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public Boolean update(UpdateRolePermissionDTO dto) {
        if (this.getById(dto.getId()) == null) {
            throw new RuntimeException("数据不存在");
        }
        RolePermission entity = new RolePermission();
        BeanUtils.copyProperties(dto, entity);
        EnumFieldMapper.mapStatusAndDeleted(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        if (this.getById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
        return this.lambdaUpdate().eq(RolePermission::getId, id).set(RolePermission::getDeleted, co.handk.common.enums.DeleteEnum.DELETED.getCode()).update();
    }

    @Override
    public PageResult<RolePermissionVO> pageQuery(RolePermissionQueryDTO query) {
        Page<RolePermission> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getDeleted, co.handk.common.enums.DeleteEnum.UNDELETED.getCode())
                .eq(query.getRoleId() != null, RolePermission::getRoleId, query.getRoleId())
                .eq(query.getPermissionId() != null, RolePermission::getPermissionId, query.getPermissionId());
        PageSortUtil.applyTimeSort(wrapper, query, RolePermission::getCreateTime, RolePermission::getUpdateTime);
        Page<RolePermission> resultPage =     rolePermissionMapper.selectPage(page, wrapper);
        List<RolePermissionVO> records = resultPage.getRecords().stream().map(entity -> {
            RolePermissionVO vo = new RolePermissionVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.build(resultPage.getTotal(), query.getPageNum(), query.getPageSize(), records);
    }
}
