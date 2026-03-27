package co.handk.backend.service;

import co.handk.backend.entity.UserToken;
import co.handk.common.model.dto.UserTokenDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface UserTokenService extends IService<UserToken> {

    Boolean create(@NotNull UserTokenDTO dto);

    UserToken get(@NotNull Long id);

    Boolean update(@NotNull UserTokenDTO dto);

    Boolean delete(@NotNull Long id);

    List<UserToken> listAll();

    PageResult<UserToken> pageQuery(@NotNull PageQuery query);
}
