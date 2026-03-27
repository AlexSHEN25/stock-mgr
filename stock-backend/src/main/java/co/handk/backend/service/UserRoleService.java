package co.handk.backend.service;

import co.handk.backend.entity.UserRole;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserRoleService extends IService<UserRole> {

    Boolean create(UserRole entity);

    UserRole get(Long id);

    Boolean update(UserRole entity);

    Boolean delete(Long id);

    List<UserRole> listAll();

    PageResult<UserRole> pageQuery(PageQuery query);
}
