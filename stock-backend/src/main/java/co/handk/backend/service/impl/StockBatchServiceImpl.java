package co.handk.backend.service.impl;

import co.handk.backend.entity.Dept;
import co.handk.backend.entity.GroupStock;
import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockReservation;
import co.handk.backend.entity.StockBatch;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.mapper.GroupStockMapper;
import co.handk.backend.mapper.StockBatchMapper;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.mapper.StockReservationMapper;
import co.handk.backend.service.DeptService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.StockBatchService;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.vo.StockBatchOptionVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockBatchServiceImpl implements StockBatchService {
    private static final long LEGACY_BATCH_ID_BASE = 9_000_000_000_000_000_000L;
    private static final DateTimeFormatter BIZ_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final StockBatchMapper stockBatchMapper;
    private final GroupStockMapper groupStockMapper;
    private final StockMapper stockMapper;
    private final StockReservationMapper stockReservationMapper;
    private final PermissionQueryService permissionQueryService;
    private final DeptService deptService;

    @Override
    public void recordInbound(StockOrder order, StockOrderItem item, Stock stock) {
        StockBatch existed = stockBatchMapper.selectOne(new QueryWrapper<StockBatch>()
                .eq("inbound_order_item_id", item.getId())
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
        batch.setCurrentQty(item.getChangeQty());
        batch.setAvailableQty(item.getChangeQty());
        batch.setAllocatedQty(0);
        batch.setCustomerOutQty(0);
        batch.setBizDate(order.getBizDate());
        batch.setSaleDeadline(order.getSaleDeadline());
        batch.setState(activeState(item.getChangeQty()));
        batch.setVersion(0L);
        stockBatchMapper.insert(batch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockOutbound(StockOrder order, StockOrderItem item, Stock stock, Long batchId) {
        if (StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(order.getOutboundMode())) {
            lockGroupStock(order, item, stock, batchId);
            return;
        }
        lockSelfStock(order, item, stock, batchId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOutbound(StockOrder order, StockOrderItem item, Stock stock) {
        List<StockReservation> locks = findLockedRows(item.getId());
        if (locks.isEmpty()) {
            throw new IllegalStateException("outbound lock rows are missing");
        }
        if (StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(order.getOutboundMode())) {
            confirmGroupCustomerLocks(locks);
            return;
        }
        confirmSelfLocks(order, stock, locks);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseOutbound(StockOrder order, StockOrderItem item, Stock stock) {
        List<StockReservation> locks = findLockedRows(item.getId());
        for (StockReservation lock : locks) {
            releaseLockedQuantity(lock, stock);
            updateLockState(lock, StockBizConstant.RESERVATION_STATE_RELEASED);
        }
    }

    @Override
    public int getSelfLockedQty(Long stockId) {
        return sumLockedQty(new QueryWrapper<StockReservation>()
                .eq("stock_id", stockId)
                .eq("reservation_scope", StockBizConstant.RESERVATION_SCOPE_SELF)
                .eq("state", StockBizConstant.RESERVATION_STATE_LOCKED));
    }

    @Override
    public int getGroupAvailableQty(Long deptId, Long goodsId, Long skuId, Long warehouseId, Long stockTypeId) {
        List<GroupStock> rows = matchingGroupRows(deptId, goodsId, skuId, warehouseId, stockTypeId);
        int total = 0;
        for (GroupStock row : rows) {
            total += Math.max(0, safeInt(row.getCurrentQty()));
        }
        return total;
    }

    @Override
    public int getGroupLockedQty(Long deptId, Long goodsId, Long skuId, Long warehouseId, Long stockTypeId) {
        QueryWrapper<StockReservation> wrapper = new QueryWrapper<StockReservation>()
                .eq("reservation_scope", StockBizConstant.RESERVATION_SCOPE_GROUP)
                .eq("goods_id", goodsId)
                .eq("sku_id", skuId)
                .eq("warehouse_id", warehouseId)
                .eq("state", StockBizConstant.RESERVATION_STATE_LOCKED);
        if (deptId != null) {
            wrapper.eq("dept_id", requireGroupDept(deptId).getId());
        }
        if (stockTypeId == null) {
            wrapper.isNull("stock_type_id");
        } else {
            wrapper.eq("stock_type_id", stockTypeId);
        }
        return sumLockedQty(wrapper);
    }

    @Override
    public List<Long> getAvailableGroupDeptIds(Long stockId) {
        if (stockId == null) {
            return List.of();
        }
        return groupStockMapper.selectList(new QueryWrapper<GroupStock>()
                        .select("dept_id")
                        .eq("stock_id", stockId)
                        .gt("current_qty", 0)
                        .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                        .and(w -> w.isNull("sale_deadline").or().ge("sale_deadline", LocalDateTime.now()))
                        .groupBy("dept_id"))
                .stream()
                .map(GroupStock::getDeptId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    @Override
    public Map<String, Integer> getGroupQuantities(Long stockId) {
        List<GroupStock> rows = groupStockMapper.selectList(new QueryWrapper<GroupStock>()
                .eq("stock_id", stockId)
                .gt("current_qty", 0)
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE));
        Map<String, Integer> result = new HashMap<>();
        for (GroupStock row : rows) {
            int availableQty = Math.max(0, safeInt(row.getCurrentQty()));
            if (availableQty <= 0) {
                continue;
            }
            result.merge(row.getDeptCode().toUpperCase(), availableQty, Integer::sum);
        }
        return result;
    }

    @Override
    public String getBizDateSummary(Long stockId, Long deptId) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (deptId == null) {
            for (StockBatch batch : activeBatches(stockId)) {
                if (batch.getBizDate() != null) {
                    values.add(batch.getBizDate().format(BIZ_DATE_FORMATTER));
                }
            }
        } else {
            for (GroupStock row : activeGroupRows(stockId, deptId)) {
                if (row.getBizDate() != null) {
                    values.add(row.getBizDate().format(BIZ_DATE_FORMATTER));
                }
            }
        }
        return String.join(", ", values);
    }

    @Override
    public List<StockBatchOptionVO> listOutboundBatchOptions(Long stockId, Long deptId) {
        List<StockBatchOptionVO> options = new ArrayList<>();
        if (stockId == null) {
            return options;
        }
        if (deptId == null) {
            for (StockBatch batch : activeBatches(stockId)) {
                int availableQty = Math.max(0, safeInt(batch.getAvailableQty()));
                if (availableQty <= 0) {
                    continue;
                }
                StockBatchOptionVO option = new StockBatchOptionVO();
                option.setBatchId(batch.getId());
                option.setBizDate(batch.getBizDate());
                option.setAvailableQty(availableQty);
                option.setSaleDeadline(batch.getSaleDeadline());
                options.add(option);
            }
            return options;
        }
        for (GroupStock row : activeGroupRows(stockId, deptId)) {
            int availableQty = Math.max(0, safeInt(row.getCurrentQty()));
            if (availableQty <= 0) {
                continue;
            }
            StockBatchOptionVO option = new StockBatchOptionVO();
            option.setBatchId(row.getBatchId());
            option.setBizDate(row.getBizDate());
            option.setAvailableQty(availableQty);
            option.setSaleDeadline(row.getSaleDeadline());
            options.add(option);
        }
        return options;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reclaimExpiredGroupStock() {
        List<GroupStock> expired = groupStockMapper.selectList(new QueryWrapper<GroupStock>()
                .gt("current_qty", 0)
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                .lt("sale_deadline", LocalDateTime.now()));
        int reclaimed = 0;
        for (GroupStock group : expired) {
            if (getLockedQtyForGroupRow(group.getId()) > 0) {
                continue;
            }
            Stock stock = stockMapper.selectById(group.getStockId());
            if (stock == null || stock.getDeleted() != DeleteEnum.UNDELETED.getCode()) {
                continue;
            }
            int qty = safeInt(group.getCurrentQty());
            int stockUpdated = stockMapper.update(null, new LambdaUpdateWrapper<Stock>()
                    .eq(Stock::getId, stock.getId())
                    .eq(Stock::getVersion, stock.getVersion())
                    .set(Stock::getCurrentQty, safeInt(stock.getCurrentQty()) + qty)
                    .set(Stock::getVersion, stock.getVersion() + 1));
            if (stockUpdated != 1) {
                throw changed();
            }
            int groupUpdated = groupStockMapper.update(null, new LambdaUpdateWrapper<GroupStock>()
                    .eq(GroupStock::getId, group.getId())
                    .eq(GroupStock::getVersion, group.getVersion())
                    .set(GroupStock::getCurrentQty, 0)
                    .set(GroupStock::getState, StockBizConstant.BATCH_STATE_EXPIRED)
                    .set(GroupStock::getVersion, group.getVersion() + 1));
            if (groupUpdated != 1) {
                throw changed();
            }
            stockBatchMapper.update(null, new LambdaUpdateWrapper<StockBatch>()
                    .eq(StockBatch::getId, group.getBatchId())
                    .setSql("current_qty = current_qty + " + qty)
                    .setSql("available_qty = available_qty + " + qty)
                    .setSql("allocated_qty = GREATEST(allocated_qty - " + qty + ", 0)")
                    .set(StockBatch::getState, StockBizConstant.BATCH_STATE_ACTIVE));
            reclaimed += qty;
        }
        return reclaimed;
    }

    private void lockSelfStock(StockOrder order, StockOrderItem item, Stock stock, Long batchId) {
        int remaining = safeInt(item.getChangeQty());
        reconcileSelfStockBatch(stock);
        if (batchId != null) {
            StockBatch batch = requireBatch(batchId);
            if (!stock.getId().equals(batch.getStockId())) {
                throw new IllegalStateException("selected batch does not belong to requested stock");
            }
            int availableQty = Math.max(0, safeInt(batch.getAvailableQty()));
            if (availableQty < remaining) {
                throw new IllegalStateException("selected inbound batch quantity is insufficient");
            }
            blockSelfQuantity(stock, batch, remaining);
            insertLockRow(order, item, stock, batch, null, null, StockBizConstant.RESERVATION_SCOPE_SELF, remaining);
            return;
        }
        List<StockBatch> batches = activeBatches(stock.getId());
        if (batches.isEmpty()) {
            throw new IllegalStateException("available inbound batch quantity is insufficient");
        }
        for (StockBatch batch : batches) {
            if (remaining <= 0) {
                break;
            }
            int availableQty = Math.max(0, safeInt(batch.getAvailableQty()));
            if (availableQty <= 0) {
                continue;
            }
            int locked = Math.min(remaining, availableQty);
            blockSelfQuantity(stock, batch, locked);
            insertLockRow(order, item, stock, batch, null, null, StockBizConstant.RESERVATION_SCOPE_SELF, locked);
            remaining -= locked;
        }
        if (remaining > 0) {
            throw new IllegalStateException("available inbound batch quantity is insufficient");
        }
    }

    private void lockGroupStock(StockOrder order, StockOrderItem item, Stock stock, Long batchId) {
        Dept dept = requireGroupDept(order.getDeptId());
        int remaining = safeInt(item.getChangeQty());
        if (batchId != null) {
            GroupStock row = groupStockMapper.selectOne(new QueryWrapper<GroupStock>()
                    .eq("stock_id", stock.getId())
                    .eq("dept_id", dept.getId())
                    .eq("batch_id", batchId)
                    .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                    .last("LIMIT 1"));
            if (row == null) {
                throw new IllegalStateException("selected group batch does not exist");
            }
            int availableQty = Math.max(0, safeInt(row.getCurrentQty()));
            if (availableQty < remaining) {
                throw new IllegalStateException("selected group batch quantity is insufficient");
            }
            blockGroupQuantity(row, remaining);
            insertLockRow(order, item, stock, null, row, dept, StockBizConstant.RESERVATION_SCOPE_GROUP, remaining);
            return;
        }
        List<GroupStock> rows = activeGroupRows(stock.getId(), dept.getId());
        for (GroupStock row : rows) {
            if (remaining <= 0) {
                break;
            }
            int availableQty = Math.max(0, safeInt(row.getCurrentQty()));
            if (availableQty <= 0) {
                continue;
            }
            int locked = Math.min(remaining, availableQty);
            blockGroupQuantity(row, locked);
            insertLockRow(order, item, stock, null, row, dept, StockBizConstant.RESERVATION_SCOPE_GROUP, locked);
            remaining -= locked;
        }
        if (remaining > 0) {
            throw new IllegalStateException("group stock is insufficient");
        }
    }

    private void blockSelfQuantity(Stock stock, StockBatch batch, int quantity) {
        if (quantity <= 0) {
            return;
        }
        int nextStockQty = safeInt(stock.getCurrentQty()) - quantity;
        if (nextStockQty < 0) {
            throw new IllegalStateException("stock quantity became negative");
        }
        int stockUpdated = stockMapper.update(null, new LambdaUpdateWrapper<Stock>()
                .eq(Stock::getId, stock.getId())
                .eq(Stock::getVersion, stock.getVersion())
                .ge(Stock::getCurrentQty, quantity)
                .set(Stock::getCurrentQty, nextStockQty)
                .set(Stock::getVersion, stock.getVersion() + 1));
        if (stockUpdated != 1) {
            throw changed();
        }
        stock.setCurrentQty(nextStockQty);
        stock.setVersion(stock.getVersion() + 1);

        int nextBatchQty = safeInt(batch.getAvailableQty()) - quantity;
        if (nextBatchQty < 0) {
            throw new IllegalStateException("stock batch available quantity became negative");
        }
        int batchUpdated = stockBatchMapper.update(null, new LambdaUpdateWrapper<StockBatch>()
                .eq(StockBatch::getId, batch.getId())
                .eq(StockBatch::getVersion, batch.getVersion())
                .ge(StockBatch::getAvailableQty, quantity)
                .set(StockBatch::getCurrentQty, nextBatchQty)
                .set(StockBatch::getAvailableQty, nextBatchQty)
                .set(StockBatch::getState, activeState(nextBatchQty))
                .set(StockBatch::getVersion, batch.getVersion() + 1));
        if (batchUpdated != 1) {
            throw changed();
        }
        batch.setCurrentQty(nextBatchQty);
        batch.setAvailableQty(nextBatchQty);
        batch.setState(activeState(nextBatchQty));
        batch.setVersion(batch.getVersion() + 1);
    }

    private void blockGroupQuantity(GroupStock row, int quantity) {
        if (quantity <= 0) {
            return;
        }
        int nextCurrent = safeInt(row.getCurrentQty()) - quantity;
        if (nextCurrent < 0) {
            throw new IllegalStateException("group stock became negative");
        }
        int updated = groupStockMapper.update(null, new LambdaUpdateWrapper<GroupStock>()
                .eq(GroupStock::getId, row.getId())
                .eq(GroupStock::getVersion, row.getVersion())
                .ge(GroupStock::getCurrentQty, quantity)
                .set(GroupStock::getCurrentQty, nextCurrent)
                .set(GroupStock::getState, activeState(nextCurrent))
                .set(GroupStock::getVersion, row.getVersion() + 1));
        if (updated != 1) {
            throw changed();
        }
        row.setCurrentQty(nextCurrent);
        row.setState(activeState(nextCurrent));
        row.setVersion(row.getVersion() + 1);
    }

    private void releaseLockedQuantity(StockReservation lock, Stock stock) {
        int quantity = safeInt(lock.getReservationQty());
        if (quantity <= 0) {
            return;
        }
        if (StockBizConstant.RESERVATION_SCOPE_GROUP.equals(lock.getReservationScope())) {
            restoreGroupQuantity(lock, quantity);
            return;
        }
        restoreSelfQuantity(lock, stock, quantity);
    }

    private void restoreSelfQuantity(StockReservation lock, Stock stock, int quantity) {
        Stock targetStock = stock != null && stock.getId().equals(lock.getStockId())
                ? stock
                : stockMapper.selectById(lock.getStockId());
        if (targetStock == null || targetStock.getDeleted() != DeleteEnum.UNDELETED.getCode()) {
            throw new IllegalStateException("stock row is missing");
        }
        int nextStockQty = safeInt(targetStock.getCurrentQty()) + quantity;
        int stockUpdated = stockMapper.update(null, new LambdaUpdateWrapper<Stock>()
                .eq(Stock::getId, targetStock.getId())
                .eq(Stock::getVersion, targetStock.getVersion())
                .set(Stock::getCurrentQty, nextStockQty)
                .set(Stock::getVersion, targetStock.getVersion() + 1));
        if (stockUpdated != 1) {
            throw changed();
        }
        targetStock.setCurrentQty(nextStockQty);
        targetStock.setVersion(targetStock.getVersion() + 1);

        StockBatch batch = requireBatch(lock.getBatchId());
        int nextBatchQty = safeInt(batch.getAvailableQty()) + quantity;
        int batchUpdated = stockBatchMapper.update(null, new LambdaUpdateWrapper<StockBatch>()
                .eq(StockBatch::getId, batch.getId())
                .eq(StockBatch::getVersion, batch.getVersion())
                .set(StockBatch::getCurrentQty, nextBatchQty)
                .set(StockBatch::getAvailableQty, nextBatchQty)
                .set(StockBatch::getState, activeState(nextBatchQty))
                .set(StockBatch::getVersion, batch.getVersion() + 1));
        if (batchUpdated != 1) {
            throw changed();
        }
    }

    private void restoreGroupQuantity(StockReservation lock, int quantity) {
        GroupStock row = requireGroupStock(lock.getGroupStockId());
        int nextCurrent = safeInt(row.getCurrentQty()) + quantity;
        int updated = groupStockMapper.update(null, new LambdaUpdateWrapper<GroupStock>()
                .eq(GroupStock::getId, row.getId())
                .eq(GroupStock::getVersion, row.getVersion())
                .set(GroupStock::getCurrentQty, nextCurrent)
                .set(GroupStock::getState, activeState(nextCurrent))
                .set(GroupStock::getVersion, row.getVersion() + 1));
        if (updated != 1) {
            throw changed();
        }
    }

    private void confirmSelfLocks(StockOrder order, Stock stock, List<StockReservation> locks) {
        Dept dept = StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE.equals(order.getOutboundMode())
                ? requireGroupDept(order.getDeptId()) : null;
        for (StockReservation lock : locks) {
            StockBatch batch = requireBatch(lock.getBatchId());
            int quantity = safeInt(lock.getReservationQty());
            if (dept != null) {
                confirmBatchAllocation(order, batch, dept, stock, quantity);
            } else {
                confirmBatchCustomerOutbound(batch, quantity);
            }
            updateLockState(lock, StockBizConstant.RESERVATION_STATE_CONFIRMED);
        }
    }

    private void confirmGroupCustomerLocks(List<StockReservation> locks) {
        for (StockReservation lock : locks) {
            updateLockState(lock, StockBizConstant.RESERVATION_STATE_CONFIRMED);
        }
    }

    private void confirmBatchCustomerOutbound(StockBatch batch, int quantity) {
        int updated = stockBatchMapper.update(null, new LambdaUpdateWrapper<StockBatch>()
                .eq(StockBatch::getId, batch.getId())
                .eq(StockBatch::getVersion, batch.getVersion())
                .set(StockBatch::getCustomerOutQty, safeInt(batch.getCustomerOutQty()) + quantity)
                .set(StockBatch::getVersion, batch.getVersion() + 1));
        if (updated != 1) {
            throw changed();
        }
    }

    private void confirmBatchAllocation(StockOrder order, StockBatch batch, Dept dept, Stock stock, int quantity) {
        int updated = stockBatchMapper.update(null, new LambdaUpdateWrapper<StockBatch>()
                .eq(StockBatch::getId, batch.getId())
                .eq(StockBatch::getVersion, batch.getVersion())
                .set(StockBatch::getAllocatedQty, safeInt(batch.getAllocatedQty()) + quantity)
                .set(StockBatch::getVersion, batch.getVersion() + 1));
        if (updated != 1) {
            throw changed();
        }

        GroupStock group = groupStockMapper.selectOne(new QueryWrapper<GroupStock>()
                .eq("batch_id", batch.getId())
                .eq("dept_id", dept.getId())
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
            group.setAllocatedQty(quantity);
            group.setCurrentQty(quantity);
            group.setBizDate(batch.getBizDate());
            group.setSaleDeadline(order.getSaleDeadline());
            group.setState(activeState(quantity));
            group.setVersion(0L);
            groupStockMapper.insert(group);
            return;
        }

        int nextCurrent = safeInt(group.getCurrentQty()) + quantity;
        int groupUpdated = groupStockMapper.update(null, new LambdaUpdateWrapper<GroupStock>()
                .eq(GroupStock::getId, group.getId())
                .eq(GroupStock::getVersion, group.getVersion())
                .set(GroupStock::getAllocatedQty, safeInt(group.getAllocatedQty()) + quantity)
                .set(GroupStock::getCurrentQty, nextCurrent)
                .set(GroupStock::getBizDate, batch.getBizDate())
                .set(GroupStock::getSaleDeadline, order.getSaleDeadline())
                .set(GroupStock::getState, activeState(nextCurrent))
                .set(GroupStock::getVersion, group.getVersion() + 1));
        if (groupUpdated != 1) {
            throw changed();
        }
    }

    private List<StockBatch> activeBatches(Long stockId) {
        return stockBatchMapper.selectList(new QueryWrapper<StockBatch>()
                .eq("stock_id", stockId)
                .gt("available_qty", 0)
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                .orderByAsc("biz_date", "sale_deadline", "id"));
    }

    private List<GroupStock> activeGroupRows(Long stockId, Long deptId) {
        return groupStockMapper.selectList(new QueryWrapper<GroupStock>()
                .eq("dept_id", deptId)
                .eq("stock_id", stockId)
                .gt("current_qty", 0)
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                .and(w -> w.isNull("sale_deadline").or().ge("sale_deadline", LocalDateTime.now()))
                .orderByAsc("biz_date", "sale_deadline", "id"));
    }

    private List<GroupStock> matchingGroupRows(Long deptId, Long goodsId, Long skuId, Long warehouseId, Long stockTypeId) {
        QueryWrapper<GroupStock> wrapper = new QueryWrapper<GroupStock>()
                .eq("goods_id", goodsId)
                .eq("sku_id", skuId)
                .eq("warehouse_id", warehouseId)
                .gt("current_qty", 0)
                .eq("state", StockBizConstant.BATCH_STATE_ACTIVE)
                .and(w -> w.isNull("sale_deadline").or().ge("sale_deadline", LocalDateTime.now()));
        if (deptId != null) {
            wrapper.eq("dept_id", requireGroupDept(deptId).getId());
        }
        if (stockTypeId == null) {
            wrapper.isNull("stock_type_id");
        } else {
            wrapper.eq("stock_type_id", stockTypeId);
        }
        return groupStockMapper.selectList(wrapper);
    }

    private List<StockReservation> findLockedRows(Long orderItemId) {
        return stockReservationMapper.selectList(new QueryWrapper<StockReservation>()
                .eq("order_item_id", orderItemId)
                .eq("state", StockBizConstant.RESERVATION_STATE_LOCKED)
                .orderByAsc("id"));
    }

    private int getLockedQtyForBatch(Long batchId) {
        return sumLockedQty(new QueryWrapper<StockReservation>()
                .eq("batch_id", batchId)
                .eq("reservation_scope", StockBizConstant.RESERVATION_SCOPE_SELF)
                .eq("state", StockBizConstant.RESERVATION_STATE_LOCKED));
    }

    private int getLockedQtyForGroupRow(Long groupStockId) {
        return sumLockedQty(new QueryWrapper<StockReservation>()
                .eq("group_stock_id", groupStockId)
                .eq("reservation_scope", StockBizConstant.RESERVATION_SCOPE_GROUP)
                .eq("state", StockBizConstant.RESERVATION_STATE_LOCKED));
    }

    private int sumLockedQty(QueryWrapper<StockReservation> wrapper) {
        wrapper.select("COALESCE(SUM(reservation_qty), 0)");
        List<Object> rows = stockReservationMapper.selectObjs(wrapper);
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            return 0;
        }
        Object value = rows.get(0);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private void insertLockRow(StockOrder order,
                               StockOrderItem item,
                               Stock stock,
                               StockBatch batch,
                               GroupStock groupStock,
                               Dept dept,
                               String reservationScope,
                               int quantity) {
        StockReservation reservation = new StockReservation();
        reservation.setIdempotencyKey(order.getIdempotencyKey());
        reservation.setOrderId(order.getId());
        reservation.setOrderItemId(item.getId());
        reservation.setStockId(stock.getId());
        reservation.setBatchId(batch == null ? null : batch.getId());
        reservation.setGroupStockId(groupStock == null ? null : groupStock.getId());
        reservation.setGoodsId(Long.valueOf(stock.getGoodsId()));
        reservation.setSkuId(stock.getSkuId());
        reservation.setWarehouseId(Long.valueOf(stock.getWarehouseId()));
        reservation.setStockTypeId(stock.getStockTypeId() == null ? 0L : stock.getStockTypeId());
        reservation.setReservationScope(reservationScope);
        reservation.setOutboundMode(order.getOutboundMode());
        reservation.setDeptId(dept == null ? order.getDeptId() : dept.getId());
        reservation.setDeptCode(dept == null ? order.getDeptCode() : dept.getCode());
        reservation.setCustomerId(order.getCustomerId());
        reservation.setCustomerName(order.getCustomerName());
        reservation.setReservationQty(quantity);
        reservation.setState(StockBizConstant.RESERVATION_STATE_LOCKED);
        reservation.setExpiresAt(batch != null ? batch.getSaleDeadline() : groupStock == null ? null : groupStock.getSaleDeadline());
        stockReservationMapper.insert(reservation);
    }

    private void updateLockState(StockReservation reservation, int state) {
        LambdaUpdateWrapper<StockReservation> wrapper = new LambdaUpdateWrapper<StockReservation>()
                .eq(StockReservation::getId, reservation.getId())
                .eq(StockReservation::getState, StockBizConstant.RESERVATION_STATE_LOCKED)
                .set(StockReservation::getState, state);
        if (state == StockBizConstant.RESERVATION_STATE_CONFIRMED) {
            wrapper.set(StockReservation::getConfirmTime, LocalDateTime.now());
        }
        if (state == StockBizConstant.RESERVATION_STATE_RELEASED) {
            wrapper.set(StockReservation::getReleaseTime, LocalDateTime.now());
        }
        int updated = stockReservationMapper.update(null, wrapper);
        if (updated != 1) {
            throw changed();
        }
    }

    /**
     * Older/imported stock rows may not have matching batch rows. Keep the
     * batch ledger aligned with the authoritative self-stock quantity before
     * consuming or allocating it.
     */
    private void reconcileSelfStockBatch(Stock stock) {
        int stockQty = safeInt(stock.getCurrentQty());
        if (stockQty <= 0) {
            return;
        }
        int batchQty = activeBatches(stock.getId()).stream()
                .mapToInt(batch -> safeInt(batch.getAvailableQty()))
                .sum();
        int missingQty = stockQty - batchQty;
        if (missingQty <= 0) {
            return;
        }

        long legacyRefId = legacyBatchRefId(stock.getId());
        StockBatch legacy = stockBatchMapper.selectOne(new QueryWrapper<StockBatch>()
                .eq("inbound_order_item_id", legacyRefId)
                .last("LIMIT 1"));
        if (legacy == null) {
            legacy = new StockBatch();
            legacy.setInboundOrderId(legacyRefId);
            legacy.setInboundOrderItemId(legacyRefId);
            legacy.setStockId(stock.getId());
            legacy.setGoodsId(Long.valueOf(stock.getGoodsId()));
            legacy.setSkuId(stock.getSkuId());
            legacy.setWarehouseId(Long.valueOf(stock.getWarehouseId()));
            legacy.setStockTypeId(stock.getStockTypeId());
            legacy.setOriginalQty(missingQty);
            legacy.setCurrentQty(missingQty);
            legacy.setAvailableQty(missingQty);
            legacy.setAllocatedQty(0);
            legacy.setCustomerOutQty(0);
            legacy.setBizDate(null);
            legacy.setSaleDeadline(null);
            legacy.setState(StockBizConstant.BATCH_STATE_ACTIVE);
            legacy.setVersion(0L);
            if (stockBatchMapper.insert(legacy) != 1) {
                throw changed();
            }
            return;
        }

        int updated = stockBatchMapper.update(null, new LambdaUpdateWrapper<StockBatch>()
                .eq(StockBatch::getId, legacy.getId())
                .eq(StockBatch::getVersion, legacy.getVersion())
                .set(StockBatch::getOriginalQty, safeInt(legacy.getOriginalQty()) + missingQty)
                .set(StockBatch::getCurrentQty, safeInt(legacy.getCurrentQty()) + missingQty)
                .set(StockBatch::getAvailableQty, safeInt(legacy.getAvailableQty()) + missingQty)
                .set(StockBatch::getState, StockBizConstant.BATCH_STATE_ACTIVE)
                .set(StockBatch::getVersion, legacy.getVersion() + 1));
        if (updated != 1) {
            throw changed();
        }
    }

    private long legacyBatchRefId(Long stockId) {
        if (stockId == null || stockId <= 0 || stockId >= LEGACY_BATCH_ID_BASE) {
            throw new IllegalArgumentException("旧在庫バッチ調整用の在庫IDが不正です");
        }
        return LEGACY_BATCH_ID_BASE + stockId;
    }

    private StockBatch requireBatch(Long batchId) {
        StockBatch batch = batchId == null ? null : stockBatchMapper.selectById(batchId);
        if (batch == null || batch.getDeleted() != DeleteEnum.UNDELETED.getCode()) {
            throw new IllegalStateException("在庫バッチが見つかりません");
        }
        return batch;
    }

    private GroupStock requireGroupStock(Long groupStockId) {
        GroupStock groupStock = groupStockId == null ? null : groupStockMapper.selectById(groupStockId);
        if (groupStock == null || groupStock.getDeleted() != DeleteEnum.UNDELETED.getCode()) {
            throw new IllegalStateException("組別在庫行が見つかりません");
        }
        return groupStock;
    }

    private Dept requireGroupDept(Long deptId) {
        Dept dept = deptId == null ? null : deptService.getByIdNotDeleted(deptId);
        if (dept == null || dept.getCode() == null || !allowedGroupCodes().contains(dept.getCode().trim().toUpperCase())) {
            throw new IllegalArgumentException("部署が在庫組として設定されていません: "
                    + (dept == null ? "null" : dept.getCode()));
        }
        return dept;
    }

    private Set<String> allowedGroupCodes() {
        return permissionQueryService.getStockGroupCodes();
    }

    private int activeState(int qty) {
        return qty > 0 ? StockBizConstant.BATCH_STATE_ACTIVE : StockBizConstant.BATCH_STATE_EXHAUSTED;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private IllegalStateException changed() {
        return new IllegalStateException("在庫が更新されました。再試行してください");
    }
}
