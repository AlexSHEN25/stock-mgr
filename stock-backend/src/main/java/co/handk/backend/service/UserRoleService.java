package co.handk.backend.service;

import co.handk.backend.entity.UserRole;
import co.handk.common.model.dto.UserRoleDTO;
import co.handk.common.model.vo.UserRoleVO;
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

    UserRoleVO get(@NotNull Long id);

    Boolean update(@NotNull UserRoleDTO dto);

    Boolean delete(@NotNull Long id);
    List<UserRoleVO> listAll();

    PageResult<UserRoleVO> pageQuery(@NotNull PageQuery query);
}
