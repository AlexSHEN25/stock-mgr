package co.handk.backend.service.impl;

import co.handk.backend.entity.Config;
import co.handk.backend.entity.Dept;
import co.handk.backend.entity.GroupStock;
import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockBatch;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.mapper.GroupStockMapper;
import co.handk.backend.mapper.StockBatchMapper;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.service.ConfigService;
import co.handk.backend.service.DeptService;
import co.handk.backend.service.StockBatchService;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockBatchServiceImpl implements StockBatchService {
    private static final String GROUP_CODES_CONFIG = "stock.group.codes";

    private final StockBatchMapper stockBatchMapper;
    private final GroupStockMapper groupStockMapper;
    private final StockMapper stockMapper;
    private final ConfigService configService;
    private final DeptService deptService;

    @Override
    public void recordInbound(StockOrder order, StockOrderItem item, Stock stock) {
        StockBatch existed = stockBatchMapper.selectOne(new QueryWrapper<StockBatch>()
                .eq("inbound_order_item_id", item.getId())
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (existed != null) {
            return;
        }
        StockBatch batch = new StockBatch();
        batch.setInboundOrderId(order.getId());
        batch.setInboundOrderItemId(item.getId());
        batch.setStockId(stock.getId());
        batch.setGoodsId(item.getGoodsId());
        batch.setSkuId(item.getSkuId());
        batch.setWarehouseId(order.getWarehouseId());
        batch.setStockTypeId(item.getStockTypeId());
        batch.setOriginalQty(item.getChangeQty());
        batch.setAvailableQty(item.getChangeQty());
        batch.setAllocatedQty(0);
        batch.setCustomerOutQty(0);
        batch.setSaleDeadline(order.getSaleDeadline());
        batch.setState(StockBizConstant.BATCH_STATE_ACTIVE);
        batch.setVersion(0L);
        stockBatchMapper.insert(batch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyOutbound(StockOrder order, StockOrderItem item, Stock stock) {
        int quantity = item.getChangeQty();
        if (StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE.equals(order.getOutboundMode())) {
            Dept dept = requireGroupDept(order.getDeptId());
            allocateToGroup(order, stock, dept, quantity);
            return;
        }
        consumeSelfBatches(stock.getId(), quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeGroupStock(StockOrder order, Stock stock, int quantity) {
        Dept dept = requireGroupDept(order.getDeptId());
        int remaining = quantity;
        List<GroupStock> rows = groupStockMapper.selectList(new QueryWrapper<GroupStock>()
                .eq("dept_id", dept.getId())
                .eq("stock_id", stock.getId())
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .and(w -> w.isNull("sale_deadline").or().ge("sale_deadline", LocalDateTime.now()))
                .orderByAsc("sale_deadline", "id"));
        for (GroupStock row : rows) {
            if (remaining <= 0) break;
            int used = Math.min(remaining, row.getCurrentQty());
            int next = row.getCurrentQty() - used;
            int updated = groupStockMapper.update(null, new LambdaUpdateWrapper<GroupStock>()
                    .eq(GroupStock::getId, row.getId())
                    .eq(GroupStock::getVersion, row.getVersion())
                    .set(GroupStock::getCurrentQty, next)
                    .set(GroupStock::getState, next == 0
                            ? StockBizConstant.BATCH_STATE_EXHAUSTED : StockBizConstant.BATCH_STATE_ACTIVE)
                    .set(GroupStock::getVersion, row.getVersion() + 1));
            if (updated != 1) throw changed();
            remaining -= used;
        }
        if (remaining > 0) {
            throw new IllegalStateException("group stock is insufficient");
        }
    }

    @Override
    public int getGroupAvailableQty(Long deptId, Long goodsId, Long skuId, Long warehouseId, Long stockTypeId) {
        Dept dept = requireGroupDept(deptId);
        QueryWrapper<GroupStock> wrapper = new QueryWrapper<GroupStock>()
                .select("COALESCE(SUM(current_qty), 0) AS current_qty")
                .eq("dept_id", dept.getId())
                .eq("goods_id", goodsId)
                .eq("sku_id", skuId)
                .eq("warehouse_id", warehouseId)
                .gt("current_qty", 0)
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .and(w -> w.isNull("sale_deadline").or().ge("sale_deadline", LocalDateTime.now()));
        if (stockTypeId == null) wrapper.isNull("stock_type_id");
        else wrapper.eq("stock_type_id", stockTypeId);
        GroupStock total = groupStockMapper.selectOne(wrapper);
        return total == null || total.getCurrentQty() == null ? 0 : total.getCurrentQty();
    }

    @Override
    public Map<String, Integer> getGroupQuantities(Long stockId) {
        List<GroupStock> rows = groupStockMapper.selectList(new QueryWrapper<GroupStock>()
                .eq("stock_id", stockId)
                .gt("current_qty", 0)
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        Map<String, Integer> result = new HashMap<>();
        for (GroupStock row : rows) {
            result.merge(row.getDeptCode().toUpperCase(), row.getCurrentQty(), Integer::sum);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reclaimExpiredGroupStock() {
        List<GroupStock> expired = groupStockMapper.selectList(new QueryWrapper<GroupStock>()
                .gt("current_qty", 0)
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .lt("sale_deadline", LocalDateTime.now()));
        int reclaimed = 0;
        for (GroupStock group : expired) {
            Stock stock = stockMapper.selectById(group.getStockId());
            if (stock == null || stock.getDeleted() != DeleteEnum.UNDELETED.getCode()) continue;
            int qty = group.getCurrentQty();
            int stockUpdated = stockMapper.update(null, new LambdaUpdateWrapper<Stock>()
                    .eq(Stock::getId, stock.getId())
                    .eq(Stock::getVersion, stock.getVersion())
                    .set(Stock::getCurrentQty, stock.getCurrentQty() + qty)
                    .set(Stock::getVersion, stock.getVersion() + 1));
            if (stockUpdated != 1) throw changed();
            int groupUpdated = groupStockMapper.update(null, new LambdaUpdateWrapper<GroupStock>()
                    .eq(GroupStock::getId, group.getId())
                    .eq(GroupStock::getVersion, group.getVersion())
                    .set(GroupStock::getCurrentQty, 0)
                    .set(GroupStock::getState, StockBizConstant.BATCH_STATE_EXPIRED)
                    .set(GroupStock::getVersion, group.getVersion() + 1));
            if (groupUpdated != 1) throw changed();
            stockBatchMapper.update(null, new LambdaUpdateWrapper<StockBatch>()
                    .eq(StockBatch::getId, group.getBatchId())
                    .setSql("available_qty = available_qty + " + qty)
                    .setSql("allocated_qty = GREATEST(allocated_qty - " + qty + ", 0)")
                    .set(StockBatch::getState, StockBizConstant.BATCH_STATE_ACTIVE));
            reclaimed += qty;
        }
        return reclaimed;
    }

    private void allocateToGroup(StockOrder order, Stock stock, Dept dept, int quantity) {
        int remaining = quantity;
        for (StockBatch batch : activeBatches(stock.getId())) {
            if (remaining <= 0) break;
            int allocated = Math.min(remaining, batch.getAvailableQty());
            updateBatch(batch, allocated, true);
            GroupStock group = groupStockMapper.selectOne(new QueryWrapper<GroupStock>()
                    .eq("batch_id", batch.getId())
                    .eq("dept_id", dept.getId())
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .last("LIMIT 1"));
            if (group == null) {
                group = new GroupStock();
                group.setBatchId(batch.getId());
                group.setDeptId(dept.getId());
                group.setDeptCode(dept.getCode());
                group.setStockId(stock.getId());
                group.setGoodsId(batch.getGoodsId());
                group.setSkuId(batch.getSkuId());
                group.setWarehouseId(batch.getWarehouseId());
                group.setStockTypeId(batch.getStockTypeId());
                group.setAllocatedQty(allocated);
                group.setCurrentQty(allocated);
                group.setSaleDeadline(batch.getSaleDeadline());
                group.setState(StockBizConstant.BATCH_STATE_ACTIVE);
                group.setVersion(0L);
                groupStockMapper.insert(group);
            } else {
                int updated = groupStockMapper.update(null, new LambdaUpdateWrapper<GroupStock>()
                        .eq(GroupStock::getId, group.getId())
                        .eq(GroupStock::getVersion, group.getVersion())
                        .set(GroupStock::getAllocatedQty, group.getAllocatedQty() + allocated)
                        .set(GroupStock::getCurrentQty, group.getCurrentQty() + allocated)
                        .set(GroupStock::getVersion, group.getVersion() + 1));
                if (updated != 1) throw changed();
            }
            remaining -= allocated;
        }
        if (remaining > 0) throw new IllegalStateException("available inbound batch quantity is insufficient");
    }

    private void consumeSelfBatches(Long stockId, int quantity) {
        int remaining = quantity;
        List<StockBatch> batches = activeBatches(stockId);
        if (batches.isEmpty()) {
            throw new IllegalStateException("available inbound batch quantity is insufficient");
        }
        for (StockBatch batch : batches) {
            if (remaining <= 0) break;
            int used = Math.min(remaining, batch.getAvailableQty());
            updateBatch(batch, used, false);
            remaining -= used;
        }
        if (remaining > 0) throw new IllegalStateException("available inbound batch quantity is insufficient");
    }

    private List<StockBatch> activeBatches(Long stockId) {
        return stockBatchMapper.selectList(new QueryWrapper<StockBatch>()
                .eq("stock_id", stockId)
                .gt("available_qty", 0)
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .and(w -> w.isNull("sale_deadline").or().ge("sale_deadline", LocalDateTime.now()))
                .orderByAsc("sale_deadline", "id"));
    }

    private void updateBatch(StockBatch batch, int quantity, boolean allocation) {
        int nextAvailable = batch.getAvailableQty() - quantity;
        int updated = stockBatchMapper.update(null, new LambdaUpdateWrapper<StockBatch>()
                .eq(StockBatch::getId, batch.getId())
                .eq(StockBatch::getVersion, batch.getVersion())
                .set(StockBatch::getAvailableQty, nextAvailable)
                .set(StockBatch::getAllocatedQty, batch.getAllocatedQty() + (allocation ? quantity : 0))
                .set(StockBatch::getCustomerOutQty, batch.getCustomerOutQty() + (allocation ? 0 : quantity))
                .set(StockBatch::getState, nextAvailable == 0
                        ? StockBizConstant.BATCH_STATE_EXHAUSTED : StockBizConstant.BATCH_STATE_ACTIVE)
                .set(StockBatch::getVersion, batch.getVersion() + 1));
        if (updated != 1) throw changed();
    }

    private Dept requireGroupDept(Long deptId) {
        Dept dept = deptId == null ? null : deptService.getByIdNotDeleted(deptId);
        if (dept == null || dept.getCode() == null || !allowedGroupCodes().contains(dept.getCode().trim().toUpperCase())) {
            throw new IllegalArgumentException("department is not configured as stock group: "
                    + (dept == null ? "null" : dept.getCode()));
        }
        return dept;
    }

    private Set<String> allowedGroupCodes() {
        Config config = configService.getOne(new QueryWrapper<Config>()
                .eq("name", GROUP_CODES_CONFIG)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        String value = config == null ? "A,B,C" : config.getValue();
        return Arrays.stream(value.split("[,，\\s\\r\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    private IllegalStateException changed() {
        return new IllegalStateException("stock changed concurrently, please retry");
    }
}
