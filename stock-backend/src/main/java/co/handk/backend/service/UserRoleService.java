package co.handk.backend.service;

import co.handk.backend.entity.UserRole;
import co.handk.common.model.dto.UserRoleDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface UserRoleService extends IService<UserRole> {

    Boolean create(@NotNull UserRoleDTO dto);

    UserRole get(@NotNull Long id);

    Boolean update(@NotNull UserRoleDTO dto);

    Boolean delete(@NotNull Long id);

    List<UserRole> listAll();

    PageResult<UserRole> pageQuery(@NotNull PageQuery query);
}
