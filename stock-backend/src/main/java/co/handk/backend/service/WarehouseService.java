package co.handk.backend.service;

import co.handk.backend.entity.Warehouse;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WarehouseService extends IService<Warehouse> {

    Boolean create(Warehouse entity);

    Warehouse get(Long id);

    Boolean update(Warehouse entity);

    Boolean delete(Long id);

    List<Warehouse> listAll();

    PageResult<Warehouse> pageQuery(PageQuery query);
}
