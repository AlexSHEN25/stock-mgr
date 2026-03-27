package co.handk.backend.service;

import co.handk.backend.entity.Permission;
import co.handk.common.model.dto.PermissionDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface PermissionService extends IService<Permission> {

    Boolean create(@NotNull PermissionDTO dto);

    Permission get(@NotNull Long id);

    Boolean update(@NotNull PermissionDTO dto);

    Boolean delete(@NotNull Long id);

    List<Permission> listAll();

    PageResult<Permission> pageQuery(@NotNull PageQuery query);
}
