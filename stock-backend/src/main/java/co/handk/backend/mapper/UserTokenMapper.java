package co.handk.backend.mapper;

import co.handk.backend.entity.UserToken;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {
    UserToken selectByToken(String token);
}
