package co.handk.backend.service;

import co.handk.backend.entity.Permission;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PermissionService extends IService<Permission> {

    Boolean create(Permission entity);

    Permission get(Long id);

    Boolean update(Permission entity);

    Boolean delete(Long id);

    List<Permission> listAll();

    PageResult<Permission> pageQuery(PageQuery query);
}
