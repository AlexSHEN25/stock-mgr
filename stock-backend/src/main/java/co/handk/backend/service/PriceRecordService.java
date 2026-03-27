package co.handk.backend.service;

import co.handk.backend.entity.PriceRecord;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PriceRecordService extends IService<PriceRecord> {

    Boolean create(PriceRecord entity);

    PriceRecord get(Long id);

    Boolean update(PriceRecord entity);

    Boolean delete(Long id);

    List<PriceRecord> listAll();

    PageResult<PriceRecord> pageQuery(PageQuery query);
}
