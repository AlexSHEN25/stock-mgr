package co.handk.backend.service;

import co.handk.backend.entity.PriceRecord;
import co.handk.common.model.vo.PriceRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface PriceRecordService extends BaseService<PriceRecord, PriceRecordVO> {
}