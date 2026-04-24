package co.handk.backend.service.impl;

import co.handk.backend.entity.User;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.service.UserService;
import co.handk.common.model.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User, UserVO>
        implements UserService {

    /**
     * Entity → VO
     */
    @Override
    protected UserVO toVO(User entity) {
        if (entity == null) return null;
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(entity, vo);

        // TODO 这里可以扩展（比如查部门名）
        // vo.setDeptName(deptService.getNameById(entity.getDeptId()));

        return vo;
    }

    /**
     * DTO → Entity
     */
    @Override
    protected <D> User toEntity(D dto) {
        if (dto == null) return null;

        User entity = new User();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}