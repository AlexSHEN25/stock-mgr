package co.handk.backend.service;

import co.handk.backend.entity.StockRecord;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StockRecordService extends IService<StockRecord> {

    Boolean create(StockRecord entity);

    StockRecord get(Long id);

    Boolean update(StockRecord entity);

    Boolean delete(Long id);

    List<StockRecord> listAll();

    PageResult<StockRecord> pageQuery(PageQuery query);
}
