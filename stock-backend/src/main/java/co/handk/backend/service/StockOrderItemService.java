package co.handk.backend.service;

import co.handk.backend.entity.StockOrderItem;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StockOrderItemService extends IService<StockOrderItem> {

    Boolean create(StockOrderItem entity);

    StockOrderItem get(Long id);

    Boolean update(StockOrderItem entity);

    Boolean delete(Long id);

    List<StockOrderItem> listAll();

    PageResult<StockOrderItem> pageQuery(PageQuery query);
}
