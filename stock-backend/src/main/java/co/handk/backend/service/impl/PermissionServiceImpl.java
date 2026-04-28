package co.handk.backend.service.impl;

import co.handk.backend.entity.Permission;
import co.handk.backend.mapper.PermissionMapper;
import co.handk.backend.service.PermissionService;
import co.handk.common.model.vo.PermissionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl extends BaseServiceImpl<PermissionMapper, Permission, PermissionVO>
        implements PermissionService {

    @Override
    protected PermissionVO toVO(Permission entity) {
        if (entity == null) {
            return null;
        }
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(entity, vo);
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
}