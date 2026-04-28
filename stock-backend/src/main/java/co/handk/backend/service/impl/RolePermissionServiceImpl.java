package co.handk.backend.service.impl;

import co.handk.backend.entity.RolePermission;
import co.handk.backend.mapper.RolePermissionMapper;
import co.handk.backend.service.RolePermissionService;
import co.handk.common.model.vo.RolePermissionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionServiceImpl extends BaseServiceImpl<RolePermissionMapper, RolePermission, RolePermissionVO>
        implements RolePermissionService {

    @Override
    protected RolePermissionVO toVO(RolePermission entity) {
        if (entity == null) {
            return null;
        }
        RolePermissionVO vo = new RolePermissionVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> RolePermission toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        RolePermission entity = new RolePermission();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}