package co.handk.backend.service;

import co.handk.backend.entity.PriceRecord;
import co.handk.common.model.dto.PriceRecordDTO;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@Validated
public interface PriceRecordService extends IService<PriceRecord> {

    Boolean create(@NotNull PriceRecordDTO dto);

    PriceRecord get(@NotNull Long id);

    Boolean update(@NotNull PriceRecordDTO dto);

    Boolean delete(@NotNull Long id);

    List<PriceRecord> listAll();

    PageResult<PriceRecord> pageQuery(@NotNull PageQuery query);
}
