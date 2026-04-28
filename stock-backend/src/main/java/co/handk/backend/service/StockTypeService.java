package co.handk.backend.service;

import co.handk.backend.entity.StockType;
import co.handk.common.model.vo.StockTypeVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface StockTypeService extends BaseService<StockType, StockTypeVO> {
}