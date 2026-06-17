package co.handk.backend.service.impl;

import co.handk.backend.entity.Permission;
import co.handk.backend.meta.EnumOptionRegistry;
import co.handk.backend.mapper.PermissionMapper;
import co.handk.backend.service.PermissionService;
import co.handk.common.constant.FieldNameConstant;
import co.handk.common.model.vo.EnumOptionVO;
import co.handk.common.model.vo.OptionVO;
import co.handk.common.model.vo.PermissionVO;
import co.handk.common.model.vo.TextOptionVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends BaseServiceImpl<PermissionMapper, Permission, PermissionVO>
        implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    protected PermissionVO toVO(Permission entity) {
        if (entity == null) {
            return null;
        }
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getParentId() != null) {
            Permission parent = permissionMapper.selectOne(new QueryWrapper<Permission>()
                    .eq(FieldNameConstant.COLUMN_ID, entity.getParentId())
                    .last("LIMIT 1"));
            if (parent != null) {
                vo.setParentName(parent.getName());
            }
        }
        return vo;
    }

    @Override
    protected <D> Permission toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Permission entity = new Permission();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    public List<OptionVO> options() {
        return permissionMapper.selectList(new QueryWrapper<Permission>()
                        .orderByAsc("sort")
                        .orderByAsc(FieldNameConstant.COLUMN_ID))
                .stream()
                .map(p -> new OptionVO(p.getId(), p.getName()))
                .toList();
    }

    @Override
    public List<TextOptionVO> moduleOptions() {
        return permissionMapper.selectList(new QueryWrapper<Permission>()
                        .select("module"))
                .stream()
                .map(Permission::getModule)
                .filter(module -> module != null && !module.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .map(module -> new TextOptionVO(module, module))
                .toList();
    }

    @Override
    public List<EnumOptionVO> typeOptions() {
        return EnumOptionRegistry.getOptions("permissionType");
    }

    @Override
    public List<EnumOptionVO> statusOptions() {
        return EnumOptionRegistry.getOptions("status");
    }
}
