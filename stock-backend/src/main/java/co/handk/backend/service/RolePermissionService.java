package co.handk.backend.service;

import co.handk.backend.entity.RolePermission;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RolePermissionService extends IService<RolePermission> {

    Boolean create(RolePermission entity);

    RolePermission get(Long id);

    Boolean update(RolePermission entity);

    Boolean delete(Long id);

    List<RolePermission> listAll();

    PageResult<RolePermission> pageQuery(PageQuery query);
}
