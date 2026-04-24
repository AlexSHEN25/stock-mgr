package co.handk.backend.service;

import co.handk.backend.entity.User;
import co.handk.common.model.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface UserService extends BaseService<User, UserVO>{

}
