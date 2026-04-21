package co.handk.backend.service;

import co.handk.backend.entity.PriceRecord;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreatePriceRecordDTO;
import co.handk.common.model.dto.query.PriceRecordQueryDTO;
import co.handk.common.model.dto.update.UpdatePriceRecordDTO;
import co.handk.common.model.vo.PriceRecordVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface PriceRecordService extends IService<PriceRecord> {
    Boolean create(@NotNull CreatePriceRecordDTO dto);
    PriceRecordVO get(@NotNull Long id);
    Boolean update(@NotNull UpdatePriceRecordDTO dto);
    Boolean delete(@NotNull Long id);
    PageResult<PriceRecordVO> pageQuery(@NotNull PriceRecordQueryDTO query);
}
