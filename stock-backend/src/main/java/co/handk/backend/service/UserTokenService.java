package co.handk.backend.service;

import co.handk.backend.entity.UserToken;
import co.handk.common.model.vo.UserTokenVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface UserTokenService extends BaseService<UserToken, UserTokenVO> {
}