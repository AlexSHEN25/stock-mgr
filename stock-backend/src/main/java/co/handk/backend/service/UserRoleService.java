package co.handk.backend.service;

import co.handk.backend.entity.UserRole;
import co.handk.common.model.vo.UserRoleVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface UserRoleService extends BaseService<UserRole, UserRoleVO> {
}