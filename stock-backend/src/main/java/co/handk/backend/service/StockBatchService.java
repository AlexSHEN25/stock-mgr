package co.handk.backend.service;

import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;

import java.util.Map;

public interface StockBatchService {
    void recordInbound(StockOrder order, StockOrderItem item, Stock stock);

    void applyOutbound(StockOrder order, StockOrderItem item, Stock stock);

    void consumeGroupStock(StockOrder order, Stock stock, int quantity);

    int getGroupAvailableQty(Long deptId, Long goodsId, Long skuId, Long warehouseId, Long stockTypeId);

    Map<String, Integer> getGroupQuantities(Long stockId);

    int reclaimExpiredGroupStock();
}
