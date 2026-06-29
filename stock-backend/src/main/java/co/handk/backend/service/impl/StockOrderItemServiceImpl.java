package co.handk.backend.service.impl;

import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.entity.StockRecord;
import co.handk.backend.entity.Warehouse;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.mapper.StockOrderItemMapper;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.StockOrderService;
import co.handk.backend.service.StockOrderItemService;
import co.handk.backend.service.StockRecordService;
import co.handk.backend.service.WarehouseService;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.query.StockOrderItemQueryDTO;
import co.handk.common.model.vo.StockOrderItemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockOrderItemServiceImpl extends BaseServiceImpl<StockOrderItemMapper, StockOrderItem, StockOrderItemVO>
        implements StockOrderItemService {

    private final StockMapper stockMapper;
    private final StockOrderService stockOrderService;
    private final StockRecordService stockRecordService;
    private final PermissionQueryService permissionQueryService;
    private final WarehouseService warehouseService;
    private static final String SELF_WAREHOUSE_CODE = "SELF";

    @Override
    protected StockOrderItemVO toVO(StockOrderItem entity) {
        if (entity == null) {
            return null;
        }
        StockOrderItemVO vo = new StockOrderItemVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> StockOrderItem toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        StockOrderItem entity = new StockOrderItem();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    protected <Q> QueryWrapper<StockOrderItem> buildWrapper(Q dto) {
        QueryWrapper<StockOrderItem> wrapper = dto instanceof StockOrderItemQueryDTO query
                ? super.buildWrapper(copyQueryWithoutStockCategory(query))
                : super.buildWrapper(dto);
        if (dto instanceof StockOrderItemQueryDTO query) {
            applyStockCategoryFilter(wrapper, query.getStockCategory());
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.inSql("order_id",
                    "SELECT id FROM t_stock_order WHERE deleted = " + DeleteEnum.UNDELETED.getCode()
                            + " AND (requester_id = "
                            + userId + " OR operator_id = " + userId + ")");
        }
        return wrapper;
    }

    private StockOrderItemQueryDTO copyQueryWithoutStockCategory(StockOrderItemQueryDTO source) {
        StockOrderItemQueryDTO copy = new StockOrderItemQueryDTO();
        BeanUtils.copyProperties(source, copy);
        copy.setStockCategory(null);
        return copy;
    }

    private void applyStockCategoryFilter(QueryWrapper<StockOrderItem> wrapper, String stockCategory) {
        String normalized = normalizeCategory(stockCategory);
        if (normalized == null) {
            return;
        }
        if ("SELF".equalsIgnoreCase(normalized) || "自社".equals(normalized)) {
            Long selfWarehouseId = findWarehouseIdByCode(SELF_WAREHOUSE_CODE);
            wrapper.inSql("order_id", "SELECT id FROM t_stock_order WHERE deleted = "
                    + DeleteEnum.UNDELETED.getCode()
                    + " AND warehouse_id = " + (selfWarehouseId == null ? -1L : selfWarehouseId));
            return;
        }
        if ("HANDLE".equalsIgnoreCase(normalized) || "柄".equals(normalized)) {
            List<Long> handleWarehouseIds = findHandleWarehouseIds();
            if (handleWarehouseIds.isEmpty()) {
                wrapper.inSql("order_id", "SELECT id FROM t_stock_order WHERE id = -1");
                return;
            }
            String ids = String.join(",", handleWarehouseIds.stream().map(String::valueOf).toList());
            wrapper.inSql("order_id", "SELECT id FROM t_stock_order WHERE deleted = "
                    + DeleteEnum.UNDELETED.getCode()
                    + " AND warehouse_id IN (" + ids + ")");
            return;
        }
        wrapper.inSql("order_id", "SELECT id FROM t_stock_order WHERE deleted = "
                + DeleteEnum.UNDELETED.getCode()
                + " AND UPPER(TRIM(COALESCE(dept_code, ''))) = '"
                + normalized.toUpperCase().replace("'", "''") + "'");
    }

    private Long findWarehouseIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        Warehouse warehouse = warehouseService.getOne(new QueryWrapper<Warehouse>()
                .eq("code", code.trim())
                .last("LIMIT 1"));
        return warehouse == null ? null : warehouse.getId();
    }

    private List<Long> findHandleWarehouseIds() {
        List<Warehouse> warehouses = warehouseService.list(new QueryWrapper<Warehouse>());
        return warehouses.stream()
                .filter(warehouse -> warehouse != null && warehouse.getId() != null && isHandleWarehouse(warehouse))
                .map(Warehouse::getId)
                .toList();
    }

    private boolean isHandleWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            return false;
        }
        String code = warehouse.getCode() == null ? "" : warehouse.getCode().trim().toUpperCase();
        String name = warehouse.getName() == null ? "" : warehouse.getName().trim();
        return code.contains("HANDLE")
                || code.contains("HAND")
                || name.contains("柄")
                || name.toUpperCase().contains("HANDLE");
    }

    private String normalizeCategory(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    @Override
    public StockOrderItem getByIdNotDeleted(Serializable id) {
        StockOrderItem item = super.getByIdNotDeleted(id);
        requireOwned(item);
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        StockOrderItem item = toEntity(dto);
        requireOwnedOrder(item == null ? null : item.getOrderId());
        boolean saved = super.saveByDto(dto);
        if (saved && item != null) {
            recalculateOrderTotalQty(item.getOrderId());
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        StockOrderItem item = toEntity(dto);
        if (item == null || item.getId() == null) {
            return super.updateByDto(dto);
        }
        StockOrderItem existed = super.getByIdNotDeleted(item.getId());
        requireOwned(existed);
        requireOwnedOrder(item.getOrderId());
        boolean updated = super.updateByDto(dto);
        if (updated) {
            recalculateOrderTotalQty(existed == null ? null : existed.getOrderId());
            if (existed != null && item.getOrderId() != null && !item.getOrderId().equals(existed.getOrderId())) {
                recalculateOrderTotalQty(item.getOrderId());
            }
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        StockOrderItem item = getByIdNotDeleted(id);
        if (item == null) {
            return 0;
        }
        rollbackStockQty(item);
        int rows = super.deleteByIdLogic(id);
        if (rows > 0) {
            rollbackOrderTotalQty(item);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchLogic(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<StockOrderItem> items = list(new QueryWrapper<StockOrderItem>()
                .in("id", ids));
        if (items == null || items.isEmpty()) {
            return 0;
        }
        Set<Long> orderIds = new HashSet<>();
        for (StockOrderItem item : items) {
            requireOwned(item);
            rollbackStockQty(item);
            orderIds.add(item.getOrderId());
        }
        int rows = super.deleteBatchLogic(ids);
        if (rows > 0) {
            for (Long orderId : orderIds) {
                recalculateOrderTotalQty(orderId);
            }
        }
        return rows;
    }

    private void rollbackStockQty(StockOrderItem item) {
        if (item == null || item.getOrderId() == null) {
            return;
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(item.getOrderId());
        if (order == null) {
            throw new RuntimeException("在庫伝票が見つかりません");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())
                && !Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())) {
            return;
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_STATE_FINISHED).equals(order.getState())) {
            return;
        }

        StockRecord record = stockRecordService.getOne(new LambdaQueryWrapper<StockRecord>()
                .eq(StockRecord::getOrderItemId, item.getId())
                .last("LIMIT 1"));
        if (record == null || record.getStockId() == null) {
            throw new RuntimeException("在庫伝票明細に対応する在庫履歴が見つかりません");
        }
        Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>()
                .eq("id", record.getStockId())
                .last("LIMIT 1"));
        if (stock == null) {
            throw new RuntimeException("在庫が見つかりません");
        }

        int qty = item.getChangeQty() == null ? 0 : item.getChangeQty();
        if (qty <= 0) {
            return;
        }
        int currentQty = stock.getCurrentQty() == null ? 0 : stock.getCurrentQty();
        int nextQty = Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())
                ? currentQty - qty
                : currentQty + qty;
        if (nextQty < 0) {
            throw new RuntimeException("在庫数量の差し戻し後に数量がマイナスになります");
        }
        long oldVersion = stock.getVersion() == null ? 0L : stock.getVersion();
        int affected = stockMapper.update(
                null,
                new LambdaUpdateWrapper<Stock>()
                .eq(Stock::getId, stock.getId())
                .eq(Stock::getVersion, oldVersion)
                .set(Stock::getCurrentQty, nextQty)
                .set(Stock::getVersion, oldVersion + 1)
        );
        if (affected <= 0) {
            throw new RuntimeException("在庫が更新されました。再試行してください");
        }
    }

    private void requireOwned(StockOrderItem item) {
        if (item == null) {
            return;
        }
        requireOwnedOrder(item.getOrderId());
    }

    private void requireOwnedOrder(Long orderId) {
        if (orderId == null) {
            return;
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        if (order == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "在庫伝票が見つかりません");
        }
    }

    private void rollbackOrderTotalQty(StockOrderItem item) {
        if (item == null || item.getOrderId() == null) {
            return;
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(item.getOrderId());
        if (order == null) {
            return;
        }
        int qty = item.getChangeQty() == null ? 0 : item.getChangeQty();
        int totalQty = order.getTotalQty() == null ? 0 : order.getTotalQty();
        int nextTotalQty = totalQty - qty;
        if (nextTotalQty < 0) {
            nextTotalQty = 0;
        }
        int affected = stockOrderService.getBaseMapper().update(
                null,
                new LambdaUpdateWrapper<StockOrder>()
                        .eq(StockOrder::getId, order.getId())
                        .eq(StockOrder::getTotalQty, totalQty)
                        .set(StockOrder::getTotalQty, nextTotalQty)
        );
        if (affected <= 0) {
            throw new RuntimeException("在庫伝票が更新されました。再試行してください");
        }
    }

    private void recalculateOrderTotalQty(Long orderId) {
        if (orderId == null) {
            return;
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        if (order == null) {
            return;
        }
        List<StockOrderItem> items = list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", orderId));
        int totalQty = 0;
        for (StockOrderItem item : items) {
            totalQty += item.getChangeQty() == null ? 0 : item.getChangeQty();
        }
        if (!stockOrderService.update(new LambdaUpdateWrapper<StockOrder>()
                .eq(StockOrder::getId, orderId)
                .set(StockOrder::getTotalQty, totalQty))) {
            throw new RuntimeException("在庫伝票の合計数量再計算に失敗しました");
        }
    }
}
