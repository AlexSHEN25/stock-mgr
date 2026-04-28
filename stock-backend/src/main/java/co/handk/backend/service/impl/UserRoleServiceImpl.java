package co.handk.backend.service.impl;

import co.handk.backend.entity.UserRole;
import co.handk.backend.mapper.UserRoleMapper;
import co.handk.backend.service.UserRoleService;
import co.handk.common.model.vo.UserRoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends BaseServiceImpl<UserRoleMapper, UserRole, UserRoleVO>
        implements UserRoleService {

    @Override
    protected UserRoleVO toVO(UserRole entity) {
        if (entity == null) {
            return null;
        }
        UserRoleVO vo = new UserRoleVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> UserRole toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        UserRole entity = new UserRole();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}