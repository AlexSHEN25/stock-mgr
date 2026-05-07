package co.handk.backend.service.impl;

import co.handk.backend.entity.Dept;
import co.handk.backend.entity.User;
import co.handk.backend.mapper.UserMapper;
import co.handk.backend.service.DeptService;
import co.handk.backend.service.UserService;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.query.UserQueryDTO;
import co.handk.common.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User, UserVO>
        implements UserService {

    private final DeptService deptService;

    /**
     * Entity → VO
     */
    @Override
    protected UserVO toVO(User entity) {
        if (entity == null) return null;
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getDeptId() != null) {
            Dept dept = deptService.getByIdNotDeleted(entity.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getName());
            }
        }

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

    @Override
    protected <Q> void buildJoinConditions(Q dto, QueryWrapper<User> wrapper) {
        if (!(dto instanceof UserQueryDTO queryDTO)) {
            return;
        }
        if (StringUtils.hasText(queryDTO.getDeptName())) {
            List<Long> deptIds = deptService.lambdaQuery()
                    .eq(Dept::getDeleted, DeleteEnum.UNDELETED.getCode())
                    .like(Dept::getName, queryDTO.getDeptName().trim())
                    .list()
                    .stream()
                    .map(Dept::getId)
                    .toList();
            if (deptIds.isEmpty()) {
                wrapper.eq("id", -1L);
            } else {
                wrapper.in("dept_id", deptIds);
            }
        }
    }
}
