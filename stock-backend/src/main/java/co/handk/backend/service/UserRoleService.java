package co.handk.backend.service;
import co.handk.backend.entity.UserRole;
import co.handk.common.model.dto.create.CreateUserRoleDTO;
import co.handk.common.model.dto.update.UpdateUserRoleDTO;
import co.handk.common.model.vo.UserRoleVO;
import co.handk.common.model.dto.query.UserRoleQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface UserRoleService extends IService<UserRole> {
    Boolean create(@NotNull CreateUserRoleDTO dto);
    UserRoleVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateUserRoleDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<UserRoleVO> pageQuery(@NotNull UserRoleQueryDTO query);
}
