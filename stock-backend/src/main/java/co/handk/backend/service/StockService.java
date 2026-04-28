package co.handk.backend.service;

import co.handk.backend.entity.Stock;
import co.handk.common.model.vo.StockVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface StockService extends BaseService<Stock, StockVO> {
}
