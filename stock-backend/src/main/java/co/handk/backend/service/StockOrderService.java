package co.handk.backend.service;

import co.handk.backend.entity.StockOrder;
import co.handk.common.model.vo.StockOrderVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface StockOrderService extends BaseService<StockOrder, StockOrderVO> {
}