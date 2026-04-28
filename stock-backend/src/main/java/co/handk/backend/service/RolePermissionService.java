package co.handk.backend.service;

import co.handk.backend.entity.RolePermission;
import co.handk.common.model.vo.RolePermissionVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface RolePermissionService extends BaseService<RolePermission, RolePermissionVO> {
}