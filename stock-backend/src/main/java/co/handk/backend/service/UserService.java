package co.handk.backend.service;

import co.handk.backend.entity.User;
import co.handk.common.model.dto.LoginDTO;
import co.handk.common.model.vo.LoginVO;
import co.handk.common.model.vo.LogoutVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends IService<User> {

    LoginVO login(LoginDTO dto);

    LogoutVO logout();
}