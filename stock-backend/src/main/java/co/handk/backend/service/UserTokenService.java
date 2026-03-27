package co.handk.backend.service;

import co.handk.backend.entity.UserToken;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserTokenService extends IService<UserToken> {

    Boolean create(UserToken entity);

    UserToken get(Long id);

    Boolean update(UserToken entity);

    Boolean delete(Long id);

    List<UserToken> listAll();

    PageResult<UserToken> pageQuery(PageQuery query);
}
