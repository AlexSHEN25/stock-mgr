package co.handk.backend.service;

import co.handk.backend.entity.Permission;
import co.handk.common.model.vo.PermissionVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface PermissionService extends BaseService<Permission, PermissionVO> {
}