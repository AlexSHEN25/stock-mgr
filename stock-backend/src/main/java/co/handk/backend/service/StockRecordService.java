package co.handk.backend.service;

import co.handk.backend.entity.StockRecord;
import co.handk.common.model.vo.StockRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface StockRecordService extends BaseService<StockRecord, StockRecordVO> {
}