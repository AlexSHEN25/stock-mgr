package co.handk.backend.service.impl;

import co.handk.backend.entity.Permission;
import co.handk.backend.entity.Role;
import co.handk.backend.entity.RolePermission;
import co.handk.backend.mapper.PermissionMapper;
import co.handk.backend.mapper.RoleMapper;
import co.handk.backend.mapper.RolePermissionMapper;
import co.handk.backend.service.RoleService;
import co.handk.common.constant.FieldNameConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.create.CreateRoleDTO;
import co.handk.common.model.dto.update.UpdateRoleDTO;
import co.handk.common.model.vo.RoleVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends BaseServiceImpl<RoleMapper, Role, RoleVO>
        implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Override
    protected RoleVO toVO(Role entity) {
        if (entity == null) {
            return null;
        }
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(entity, vo);
        fillPermissions(entity.getId(), vo);
        return vo;
    }

    @Override
    protected <D> Role toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Role entity = new Role();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateRoleDTO createDto) {
            Role role = toEntity(dto);
            boolean saved = this.save(role);
            if (!saved) {
                return false;
            }
            syncRolePermissions(role.getId(), resolvePermissionIds(createDto.getPermissionIds(), createDto.getPermissionNames()));
            return true;
        }
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof UpdateRoleDTO updateDto) {
            boolean updated = super.updateByDto(dto);
            if (!updated) {
                return false;
            }
            syncRolePermissions(updateDto.getId(), resolvePermissionIds(updateDto.getPermissionIds(), updateDto.getPermissionNames()));
            return true;
        }
        return super.updateByDto(dto);
    }

    private void fillPermissions(Long roleId, RoleVO vo) {
        if (roleId == null || vo == null) {
            return;
        }
        List<RolePermission> relations = rolePermissionMapper.selectList(new QueryWrapper<RolePermission>()
                .eq("role_id", roleId)
                .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode()));
        if (relations == null || relations.isEmpty()) {
            vo.setPermissionIds(new ArrayList<>());
            vo.setPermissionNames(new ArrayList<>());
            return;
        }

        List<Long> permissionIds = relations.stream()
                .map(RolePermission::getPermissionId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        vo.setPermissionIds(permissionIds);

        if (permissionIds.isEmpty()) {
            vo.setPermissionNames(new ArrayList<>());
            return;
        }

        List<Permission> permissions = permissionMapper.selectList(new QueryWrapper<Permission>()
                .in(FieldNameConstant.COLUMN_ID, permissionIds)
                .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode()));
        if (permissions == null || permissions.isEmpty()) {
            vo.setPermissionNames(new ArrayList<>());
            return;
        }

        Map<Long, String> nameMap = permissions.stream()
                .collect(Collectors.toMap(Permission::getId, Permission::getName, (a, b) -> a));

        List<String> permissionNames = permissionIds.stream()
                .map(nameMap::get)
                .filter(name -> name != null && !name.isBlank())
                .toList();
        vo.setPermissionNames(permissionNames);
    }

    private List<Long> resolvePermissionIds(List<Long> permissionIds, List<String> permissionNames) {
        if (permissionIds != null && !permissionIds.isEmpty()) {
            return permissionIds.stream().filter(id -> id != null).distinct().toList();
        }
        if (permissionNames == null || permissionNames.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> normalizedNames = permissionNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (normalizedNames.isEmpty()) {
            return new ArrayList<>();
        }
        return permissionMapper.selectList(new QueryWrapper<Permission>()
                        .in("name", normalizedNames)
                        .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode()))
                .stream()
                .map(Permission::getId)
                .filter(id -> id != null)
                .distinct()
                .toList();
    }

    private void syncRolePermissions(Long roleId, List<Long> permissionIds) {
        if (roleId == null) {
            return;
        }

        List<RolePermission> existed = rolePermissionMapper.selectList(new QueryWrapper<RolePermission>()
                .eq("role_id", roleId)
                .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode()));

        Set<Long> targetSet = permissionIds == null
                ? new LinkedHashSet<>()
                : permissionIds.stream().filter(id -> id != null).collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> existedSet = existed.stream()
                .map(RolePermission::getPermissionId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> toDelete = new LinkedHashSet<>(existedSet);
        toDelete.removeAll(targetSet);

        Set<Long> toAdd = new LinkedHashSet<>(targetSet);
        toAdd.removeAll(existedSet);

        if (!toDelete.isEmpty()) {
            rolePermissionMapper.update(null, new UpdateWrapper<RolePermission>()
                    .eq("role_id", roleId)
                    .in("permission_id", toDelete)
                    .eq(FieldNameConstant.COLUMN_DELETED, DeleteEnum.UNDELETED.getCode())
                    .set(FieldNameConstant.COLUMN_DELETED, DeleteEnum.DELETED.getCode()));
        }

        for (Long permissionId : toAdd) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rolePermissionMapper.insert(rp);
        }
    }
}
