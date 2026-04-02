package co.handk.backend.service;
import co.handk.backend.entity.Permission;
import co.handk.common.model.dto.create.CreatePermissionDTO;
import co.handk.common.model.dto.update.UpdatePermissionDTO;
import co.handk.common.model.vo.PermissionVO;
import co.handk.common.model.dto.query.PermissionQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface PermissionService extends IService<Permission> {
    Boolean create(@NotNull CreatePermissionDTO dto);
    PermissionVO get(@NotNull Long id);
    Boolean update(@NotNull UpdatePermissionDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<PermissionVO> pageQuery(@NotNull PermissionQueryDTO query);
}
