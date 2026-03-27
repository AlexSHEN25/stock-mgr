package co.handk.backend.service;

import co.handk.backend.entity.StockRecord;
import co.handk.common.model.dto.StockRecordDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface StockRecordService extends IService<StockRecord> {

    Boolean create(@NotNull StockRecordDTO dto);

    StockRecord get(@NotNull Long id);

    Boolean update(@NotNull StockRecordDTO dto);

    Boolean delete(@NotNull Long id);

    List<StockRecord> listAll();

    PageResult<StockRecord> pageQuery(@NotNull PageQuery query);
}
