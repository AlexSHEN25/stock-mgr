package co.handk.backend.service;

import co.handk.backend.entity.Role;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleService extends IService<Role> {

    Boolean create(Role entity);

    Role get(Long id);

    Boolean update(Role entity);

    Boolean delete(Long id);

    List<Role> listAll();

    PageResult<Role> pageQuery(PageQuery query);
}
