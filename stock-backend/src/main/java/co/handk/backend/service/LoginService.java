package co.handk.backend.service;

import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface LoginService {

    LoginVO login(@NotNull LoginDTO dto);
    LogoutVO logout();
}
