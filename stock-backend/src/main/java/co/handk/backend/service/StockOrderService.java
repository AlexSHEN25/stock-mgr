package co.handk.backend.service;

import co.handk.backend.entity.StockOrder;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StockOrderService extends IService<StockOrder> {

    Boolean create(StockOrder entity);

    StockOrder get(Long id);

    Boolean update(StockOrder entity);

    Boolean delete(Long id);

    List<StockOrder> listAll();

    PageResult<StockOrder> pageQuery(PageQuery query);
}
