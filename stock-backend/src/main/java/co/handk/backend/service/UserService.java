package co.handk.backend.service;

import co.handk.backend.entity.User;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.dto.UserDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import co.handk.common.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface UserService extends IService<User> {

    Boolean create(@NotNull UserDTO dto);

    UserVO get(@NotNull Long id);

    Boolean update(@NotNull UserDTO dto);

    Boolean delete(@NotNull Long id);

    List<UserVO> listAll();

    LoginVO login(@NotNull LoginDTO dto);

    LogoutVO logout();

    PageResult<UserVO> pageQuery(@NotNull PageQuery query);
}
