package co.handk.backend.service.impl;

import co.handk.backend.entity.Role;
import co.handk.backend.mapper.RoleMapper;
import co.handk.backend.service.RoleService;
import co.handk.common.model.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends BaseServiceImpl<RoleMapper, Role, RoleVO>
        implements RoleService {

    @Override
    protected RoleVO toVO(Role entity) {
        if (entity == null) {
            return null;
        }
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(entity, vo);
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
}