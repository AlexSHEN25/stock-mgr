package co.handk.backend.service;

import co.handk.backend.entity.User;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public interface UserService extends IService<User> {

    LoginVO login(@NotNull LoginDTO dto);

    LogoutVO logout();

    PageResult<User> pageQuery(@NotNull PageQuery query);
}
