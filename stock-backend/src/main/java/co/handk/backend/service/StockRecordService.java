package co.handk.backend.service;
import co.handk.backend.entity.StockRecord;
import co.handk.common.model.dto.create.CreateStockRecordDTO;
import co.handk.common.model.dto.update.UpdateStockRecordDTO;
import co.handk.common.model.vo.StockRecordVO;
import co.handk.common.model.dto.query.StockRecordQueryDTO;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
@Service
@Validated
public interface StockRecordService extends IService<StockRecord> {
    Boolean create(@NotNull CreateStockRecordDTO dto);
    StockRecordVO get(@NotNull Long id);
    Boolean update(@NotNull UpdateStockRecordDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<StockRecordVO> pageQuery(@NotNull StockRecordQueryDTO query);
}
