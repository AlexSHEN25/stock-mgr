package co.handk.backend.service;
import co.handk.backend.entity.UserToken;
import co.handk.common.model.dto.create.CreateUserTokenDTO;
import co.handk.common.model.dto.update.UpdateUserTokenDTO;
import co.handk.common.model.vo.UserTokenVO;
import co.handk.common.model.dto.query.UserTokenQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface UserTokenService extends IService<UserToken> {
    Boolean create(@NotNull CreateUserTokenDTO dto);
    UserTokenVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateUserTokenDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<UserTokenVO> pageQuery(@NotNull UserTokenQueryDTO query);
}
