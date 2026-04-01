package co.handk.backend.service;

import co.handk.backend.entity.RolePermission;
import co.handk.common.model.dto.RolePermissionDTO;
import co.handk.common.model.vo.RolePermissionVO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface RolePermissionService extends IService<RolePermission> {

    Boolean create(@NotNull RolePermissionDTO dto);

    RolePermissionVO get(@NotNull Long id);

    Boolean update(@NotNull RolePermissionDTO dto);

    Boolean delete(@NotNull Long id);
    List<RolePermissionVO> listAll();

    PageResult<RolePermissionVO> pageQuery(@NotNull PageQuery query);
}
