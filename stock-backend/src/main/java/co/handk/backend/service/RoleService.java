package co.handk.backend.service;

import co.handk.backend.entity.Role;
import co.handk.common.model.dto.RoleDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface RoleService extends IService<Role> {

    Boolean create(@NotNull RoleDTO dto);

    Role get(@NotNull Long id);

    Boolean update(@NotNull RoleDTO dto);

    Boolean delete(@NotNull Long id);

    List<Role> listAll();

    PageResult<Role> pageQuery(@NotNull PageQuery query);
}
