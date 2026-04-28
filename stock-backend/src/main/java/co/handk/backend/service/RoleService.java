package co.handk.backend.service;

import co.handk.backend.entity.Role;
import co.handk.common.model.vo.RoleVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface RoleService extends BaseService<Role, RoleVO> {
}