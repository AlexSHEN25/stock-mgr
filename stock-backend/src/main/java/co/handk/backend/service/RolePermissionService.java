package co.handk.backend.service;

import co.handk.backend.entity.RolePermission;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRolePermissionDTO;
import co.handk.common.model.dto.query.RolePermissionQueryDTO;
import co.handk.common.model.dto.update.UpdateRolePermissionDTO;
import co.handk.common.model.vo.RolePermissionVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface RolePermissionService extends IService<RolePermission> {
    Boolean create(@NotNull CreateRolePermissionDTO dto);
    RolePermissionVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateRolePermissionDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<RolePermissionVO> pageQuery(@NotNull RolePermissionQueryDTO query);
}
