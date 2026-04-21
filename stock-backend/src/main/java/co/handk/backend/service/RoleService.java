package co.handk.backend.service;

import co.handk.backend.entity.Role;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRoleDTO;
import co.handk.common.model.dto.query.RoleQueryDTO;
import co.handk.common.model.dto.update.UpdateRoleDTO;
import co.handk.common.model.vo.RoleVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface RoleService extends IService<Role> {
    Boolean create(@NotNull CreateRoleDTO dto);
    RoleVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateRoleDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<RoleVO> pageQuery(@NotNull RoleQueryDTO query);
}
