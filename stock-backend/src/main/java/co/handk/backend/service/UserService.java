package co.handk.backend.service;

import co.handk.backend.entity.User;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.dto.create.CreateUserDTO;
import co.handk.common.model.dto.query.UserQueryDTO;
import co.handk.common.model.dto.update.UpdateUserDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.model.vo.UserVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface UserService extends BaseService<User> {
    LoginVO login(@NotNull LoginDTO dto);
    LogoutVO logout();
    Boolean create(@NotNull CreateUserDTO dto);
    UserVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateUserDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<UserVO> pageQuery(@NotNull UserQueryDTO query);
}
