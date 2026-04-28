package co.handk.backend.service;

import co.handk.backend.entity.StockOrderItem;
import co.handk.common.model.vo.StockOrderItemVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public interface StockOrderItemService extends BaseService<StockOrderItem, StockOrderItemVO> {
}