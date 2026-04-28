package co.handk.backend.service.impl;

import co.handk.backend.entity.UserToken;
import co.handk.backend.mapper.UserTokenMapper;
import co.handk.backend.service.UserTokenService;
import co.handk.common.model.vo.UserTokenVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserTokenServiceImpl extends BaseServiceImpl<UserTokenMapper, UserToken, UserTokenVO>
        implements UserTokenService {

    @Override
    protected UserTokenVO toVO(UserToken entity) {
        if (entity == null) {
            return null;
        }
        UserTokenVO vo = new UserTokenVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> UserToken toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        UserToken entity = new UserToken();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}