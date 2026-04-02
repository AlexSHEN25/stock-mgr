package co.handk.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import co.handk.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from t_user where username = #{username} and status=1 and deleted = 0 limit 1")
    User selectByUsername(@Param("username") String username);
}
