package co.handk.backend.service;

import co.handk.backend.entity.GroupStock;
import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;

import java.util.Map;
import java.util.List;

public interface StockBatchService {
    void recordInbound(StockOrder order, StockOrderItem item, Stock stock);

    void lockOutbound(StockOrder order, StockOrderItem item, Stock stock);

    void confirmOutbound(StockOrder order, StockOrderItem item, Stock stock);

    void releaseOutbound(StockOrder order, StockOrderItem item, Stock stock);

    int getSelfLockedQty(Long stockId);

    int getGroupAvailableQty(Long deptId, Long goodsId, Long skuId, Long warehouseId, Long stockTypeId);

    int getGroupLockedQty(Long deptId, Long goodsId, Long skuId, Long warehouseId, Long stockTypeId);

    List<Long> getAvailableGroupDeptIds(Long stockId);

    Map<String, Integer> getGroupQuantities(Long stockId);

    int reclaimExpiredGroupStock();
}
