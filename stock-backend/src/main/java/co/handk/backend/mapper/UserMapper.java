package co.handk.backend.mapper;

import co.handk.backend.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from t_user where username = #{username} and status = #{status} and deleted = #{deleted} limit 1")
    User selectByUsername(@Param("username") String username,
                          @Param("status") Integer status,
                          @Param("deleted") Integer deleted);
}
