package co.handk.backend.service.impl;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.*;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.service.*;
import co.handk.backend.util.StringRedisUtil;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.RedisKey;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockDTO;
import co.handk.common.model.dto.create.StockOperateDTO;
import co.handk.common.model.dto.create.StockOrderSubmitDTO;
import co.handk.common.model.dto.create.StockOrderSubmitItemDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.vo.StockVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl extends BaseServiceImpl<StockMapper, Stock, StockVO> implements StockService {

    private static final int MESSAGE_TYPE_INBOUND = 1;
    private static final int MESSAGE_TYPE_WARNING = 2;
    private static final int MESSAGE_IS_UNREAD = 0;
    private static final int MESSAGE_STATE_SENT = 1;
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final String GROUP_CODES_CONFIG = "stock.group.codes";

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private StockTypeService stockTypeService;
    @Autowired
    private StockOrderService stockOrderService;
    @Autowired
    private StockOrderItemService stockOrderItemService;
    @Autowired
    private StockRecordService stockRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private PriceRecordService priceRecordService;
    @Autowired
    private PermissionQueryService permissionQueryService;
    @Autowired
    private StockBatchService stockBatchService;
    @Autowired
    private DeptService deptService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private StringRedisUtil stringRedisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateStockDTO createDto) {
            createDto.setLockQty(0);
        }
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (!(dto instanceof UpdateStockDTO updateDto)) {
            return super.updateByDto(dto);
        }
        Stock existed = this.getByIdNotDeleted(updateDto.getId());
        if (existed == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫が存在しません");
        }

        java.math.BigDecimal oldPrice = existed.getPrice();
        java.math.BigDecimal newPrice = updateDto.getPrice();
        updateDto.setLockQty(existed.getLockQty());
        boolean priceChanged = newPrice != null && (oldPrice == null || oldPrice.compareTo(newPrice) != 0);
        if (priceChanged && updateDto.getPriceUpdateTime() == null) {
            updateDto.setPriceUpdateTime(LocalDateTime.now());
        }

        boolean updated = super.updateByDto(updateDto);
        if (!updated || !priceChanged) {
            return updated;
        }

        PriceRecord record = new PriceRecord();
        record.setGoodsId(Long.valueOf(updateDto.getGoodsId()));
        record.setGoodsName(updateDto.getGoodsName());
        record.setEnglishName(null);
        record.setSkuId(updateDto.getSkuId());
        record.setSkuCode(updateDto.getSkuCode());
        record.setOldPrice(oldPrice);
        record.setNewPrice(newPrice);
        record.setCurrency(updateDto.getCurrency());
        record.setDiscount(null);
        record.setPriceUpdateTime(updateDto.getPriceUpdateTime());
        Long operatorId = UserContext.getUserIdOrDefault();
        record.setOperatorId(operatorId);
        User operator = userService.getByIdNotDeleted(operatorId);
        record.setOperatorName(operator == null ? null : operator.getUsername());
        if (!priceRecordService.save(record)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "価格履歴の保存に失敗しました");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long inbound(StockOperateDTO dto) {
        Stock stock = resolveInboundStock(dto);
        Goods goods = requireGoods(stock.getGoodsId());
        GoodsSku sku = requireSku(stock.getSkuId(), goods.getId());
        String stockTypeName = getStockTypeName(stock.getStockTypeId());

        int scene = resolveInboundScene(dto.getSourceType());
        int beforeQty = safeInt(stock.getCurrentQty());
        int afterQty = beforeQty + dto.getQuantity();

        int sourceType = scene == StockBizConstant.INBOUND_SCENE_SELF
                ? StockBizConstant.SOURCE_TYPE_REQUEST : StockBizConstant.SOURCE_TYPE_MANUAL;
        boolean needApprove = scene == StockBizConstant.INBOUND_SCENE_SELF;

        StockOrder order = saveStockOrder(stock, dto, StockBizConstant.ORDER_TYPE_INBOUND, sourceType,
                needApprove ? StockBizConstant.ORDER_STATE_APPROVING : StockBizConstant.ORDER_STATE_FINISHED);
        StockOrderItem item = saveOrderItem(order.getId(), goods, sku, stock, stockTypeName, dto, beforeQty, afterQty);
        if (!needApprove) {
            updateStockQuantityWithVersion(stock, afterQty);
            saveStockRecord(order, stock, dto.getRemark(), beforeQty, afterQty);
            stockBatchService.recordInbound(order, item, stock);
            notifyInbound(sku.getSkuCode(), dto.getQuantity(), afterQty, order.getId());
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long outbound(StockOperateDTO dto) {
        prepareOutboundAccess(dto);
        Stock stock = resolveOutboundStock(dto);
        requireAccessibleOutboundStock(stock, dto.getOutboundMode());
        Goods goods = requireGoods(stock.getGoodsId());
        GoodsSku sku = requireSku(stock.getSkuId(), goods.getId());
        String stockTypeName = getStockTypeName(stock.getStockTypeId());
        requireOutboundIdempotency(dto, stock);

        boolean groupCustomer = StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER
                .equals(normalizeOutboundMode(dto.getOutboundMode()));
        int beforeQty = groupCustomer
                ? stockBatchService.getGroupAvailableQty(dto.getDeptId(), Long.valueOf(stock.getGoodsId()),
                stock.getSkuId(), Long.valueOf(stock.getWarehouseId()), stock.getStockTypeId())
                : safeInt(stock.getCurrentQty());
        if (beforeQty < dto.getQuantity()) {
            notifyInsufficientStock(sku.getSkuCode(), dto.getQuantity(), beforeQty, stock.getId());
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫数量が不足しています");
        }
        int afterQty = beforeQty - dto.getQuantity();

        StockOrder order = saveStockOrder(stock, dto, StockBizConstant.ORDER_TYPE_OUTBOUND,
                StockBizConstant.SOURCE_TYPE_MANUAL, StockBizConstant.ORDER_STATE_APPROVING);
        saveOrderItem(order.getId(), goods, sku, stock, stockTypeName, dto, beforeQty, afterQty);
        return order.getId();
    }

    @Override
    public <Q extends co.handk.common.model.PageQuery> PageResult<StockVO> page(Q dto) {
        if (!(dto instanceof StockQueryDTO query)) {
            return super.page(dto);
        }
        String scope = query.getStockScope() == null ? null : query.getStockScope().trim().toLowerCase();
        if ("self".equals(scope)) {
            query.setWarehouseId(requireWarehouseByCode("SELF").getId());
            return super.page(query);
        }
        if ("group".equals(scope)) {
            Dept dept = resolveGroupDeptForScope(query.getGroupCode());
            QueryWrapper<Stock> wrapper = buildGroupStockPageWrapper(query, dept);
            return pageStockWithWrapper(query, wrapper);
        }
        return super.page(query);
    }

    @Override
    public Integer getMyGroupAvailableQty(Long goodsId, Long skuId, Long warehouseId, Long stockTypeId) {
        Dept dept = resolveAccessibleGroupDept(null);
        return stockBatchService.getGroupAvailableQty(
                dept.getId(), goodsId, skuId, warehouseId, stockTypeId);
    }

    private Long currentDeptId() {
        User user = userService.getByIdNotDeleted(UserContext.getUserIdOrDefault());
        return user == null ? null : user.getDeptId();
    }

    private Dept resolveGroupDeptForScope(String groupCode) {
        Long userId = UserContext.getUserIdOrDefault();
        boolean admin = permissionQueryService.isSuperAdmin(userId);
        if (groupCode != null && !groupCode.isBlank()) {
            Dept requested = deptService.getOne(new QueryWrapper<Dept>()
                    .eq("code", groupCode.trim())
                    .eq("deleted", DeleteEnum.UNDELETED.getCode())
                    .last("LIMIT 1"));
            if (requested == null) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "department is not configured: " + groupCode);
            }
            if (!admin) {
                Long userDeptId = currentDeptId();
                if (userDeptId == null || !userDeptId.equals(requested.getId())) {
                    throw new co.handk.backend.exception.BusinessException(
                            co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                            "users can only view stock for their own department");
                }
            }
            return requested;
        }
        return resolveAccessibleGroupDept(null);
    }

    private QueryWrapper<Stock> buildGroupStockPageWrapper(StockQueryDTO query, Dept dept) {
        QueryWrapper<Stock> wrapper = buildWrapper(query)
                .inSql("id", "SELECT stock_id FROM t_group_stock"
                        + " WHERE deleted = 0 AND state = " + StockBizConstant.BATCH_STATE_ACTIVE
                        + " AND dept_id = " + dept.getId())
                .orderByDesc("update_time");
        return wrapper;
    }

    private PageResult<StockVO> pageStockWithWrapper(StockQueryDTO query, QueryWrapper<Stock> wrapper) {
        Page<Stock> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Stock> result = this.page(page, wrapper);
        List<StockVO> records = result.getRecords().stream()
                .map(this::toVO)
                .peek(this::fillStatusDesc)
                .toList();
        fillStockJoins(records);
        return PageResult.build(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    private void fillStockJoins(List<StockVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (StockVO vo : records) {
            if (vo == null) {
                continue;
            }
            if (vo.getGoodsId() != null) {
                Goods goods = goodsService.getByIdNotDeleted(Long.valueOf(vo.getGoodsId()));
                vo.setGoodsName(goods == null ? null : goods.getName());
            }
            if (vo.getWarehouseId() != null) {
                Warehouse warehouse = warehouseService.getByIdNotDeleted(Long.valueOf(vo.getWarehouseId()));
                vo.setWarehouseName(warehouse == null ? null : warehouse.getName());
            }
            if (vo.getStockTypeId() != null) {
                StockType stockType = stockTypeService.getByIdNotDeleted(vo.getStockTypeId());
                vo.setStockTypeName(stockType == null ? null : stockType.getName());
            }
        }
    }

    private void prepareOutboundAccess(StockOperateDTO dto) {
        String mode = normalizeOutboundMode(dto.getOutboundMode());
        dto.setOutboundMode(mode);
        Long userId = UserContext.getUserIdOrDefault();
        boolean admin = permissionQueryService.isSuperAdmin(userId);

        if (StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE.equals(mode)) {
            if (!admin) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "only administrators can allocate self stock to a group");
            }
            dto.setDeptId(resolveAccessibleGroupDept(dto.getDeptId()).getId());
            return;
        }

        if (StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(mode)) {
            dto.setDeptId(resolveAccessibleGroupDept(dto.getDeptId()).getId());
            return;
        }

        if (!admin) {
            dto.setDeptId(null);
        }
    }

    private void requireAccessibleOutboundStock(Stock stock, String outboundMode) {
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)
                || StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(outboundMode)) {
            return;
        }
        Warehouse selfWarehouse = requireWarehouseByCode("SELF");
        if (stock.getWarehouseId() == null || !selfWarehouse.getId().equals(Long.valueOf(stock.getWarehouseId()))) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "users can only request outbound from self stock or their own group stock");
        }
    }

    private Warehouse requireWarehouseByCode(String code) {
        Warehouse warehouse = warehouseService.getOne(new QueryWrapper<Warehouse>()
                .eq("code", code)
                .eq("status", StatusEnum.NOMAL.getCode())
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (warehouse == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "required warehouse is not configured: " + code);
        }
        return warehouse;
    }

    private Dept resolveAccessibleGroupDept(Long requestedDeptId) {
        Long userId = UserContext.getUserIdOrDefault();
        boolean admin = permissionQueryService.isSuperAdmin(userId);
        Long userDeptId = currentDeptId();
        Long targetDeptId;
        if (admin) {
            targetDeptId = requestedDeptId;
        } else {
            if (requestedDeptId != null && !requestedDeptId.equals(userDeptId)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "users can only operate stock for their own department");
            }
            targetDeptId = userDeptId;
        }
        if (targetDeptId == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "a stock group department is required");
        }
        Dept dept = deptService.getByIdNotDeleted(targetDeptId);
        if (dept == null || dept.getCode() == null || !isConfiguredGroupCode(dept.getCode())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "department is not configured as stock group: " + (dept == null ? "null" : dept.getCode()));
        }
        return dept;
    }

    private boolean isConfiguredGroupCode(String deptCode) {
        Config config = configService.getOne(new QueryWrapper<Config>()
                .eq("name", GROUP_CODES_CONFIG)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        String value = config == null || config.getValue() == null || config.getValue().isBlank()
                ? "A,B,C" : config.getValue();
        for (String code : value.split("[,，\\s\\n\\r]+")) {
            if (deptCode != null && deptCode.trim().equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveOrder(Long orderId, Boolean approved, String approveRemark) {
        Long approverId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(approverId)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "only administrators can approve stock orders");
        }
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        if (order == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票が存在しません");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())
                && !Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票ではありません");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_STATE_APPROVING).equals(order.getState())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "承認待ち状態ではありません");
        }

        order.setApproverId(approverId);
        User approver = userService.getByIdNotDeleted(order.getApproverId());
        order.setApproverName(approver == null ? null : approver.getUsername());
        order.setApproveTime(LocalDateTime.now());
        order.setRemark(approveRemark);

        if (Boolean.FALSE.equals(approved)) {
            order.setState(StockBizConstant.ORDER_STATE_CANCELED);
            if (!stockOrderService.updateById(order)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "伝票更新に失敗しました");
            }
            return true;
        }

        List<StockOrderItem> items = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", orderId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (items == null || items.isEmpty()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票明細が存在しません");
        }

        for (StockOrderItem item : items) {
            Stock stock = findStock(item.getGoodsId(), item.getSkuId(), order.getWarehouseId(), item.getStockTypeId());
            if (stock == null) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫商品が存在しません");
            }

            int changeQty = safeInt(item.getChangeQty());
            boolean groupAllocate = Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())
                    && StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE.equals(order.getOutboundMode());
            boolean groupCustomer = Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())
                    && StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(order.getOutboundMode());
            int beforeQty = groupAllocate
                    ? safeInt(stock.getCurrentQty())
                    : groupCustomer
                    ? stockBatchService.getGroupAvailableQty(order.getDeptId(), item.getGoodsId(), item.getSkuId(),
                    order.getWarehouseId(), item.getStockTypeId())
                    : safeInt(stock.getCurrentQty());
            int afterQty = (groupAllocate || groupCustomer) ? beforeQty - changeQty
                    : Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())
                    ? beforeQty + changeQty : beforeQty - changeQty;
            if (afterQty < 0) {
                notifyInsufficientStock(item.getSkuCode(), changeQty, beforeQty, stock.getId());
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                        "insufficient stock for approved outbound order: SKU[" + item.getSkuCode() + "]");
            }
            if (groupAllocate) {
                stockBatchService.applyOutbound(order, item, stock);
                updateStockQuantityWithVersion(stock, afterQty);
            } else if (groupCustomer) {
                stockBatchService.consumeGroupStock(order, stock, changeQty);
            } else {
                if (Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(order.getOrderType())) {
                    stockBatchService.applyOutbound(order, item, stock);
                }
                updateStockQuantityWithVersion(stock, afterQty);
            }
            item.setBeforeQty(beforeQty);
            item.setAfterQty(afterQty);
            if (!stockOrderItemService.updateById(item)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫更新に失敗しました");
            }

            StockRecord record = new StockRecord();
            record.setBizNo(order.getOrderNo());
            record.setOrderId(order.getId());
            record.setOrderItemId(item.getId());
            record.setStockId(stock.getId());
            record.setGoodsId(item.getGoodsId());
            record.setSkuId(item.getSkuId());
            record.setSkuCode(item.getSkuCode());
            record.setGoodsName(item.getGoodsName());
            record.setEnglishName(item.getEnglishName());
            record.setBrandId(item.getBrandId());
            record.setBrandName(item.getBrandName());
            record.setSeriesId(item.getSeriesId());
            record.setSeriesName(item.getSeriesName());
            record.setCategoryId(item.getCategoryId());
            record.setCategoryName(item.getCategoryName());
            record.setStockTypeId(item.getStockTypeId());
            record.setStockTypeName(item.getStockTypeName());
            record.setMakerId(item.getMakerId());
            record.setMakerName(item.getMakerName());
            record.setWarehouseId(order.getWarehouseId());
            record.setBeforeQty(beforeQty);
            record.setChangeQty(item.getChangeQty());
            record.setAfterQty(afterQty);
            record.setOrderType(order.getOrderType());
            record.setSourceType(order.getSourceType());
            record.setPrice(item.getPrice());
            record.setCurrency(item.getCurrency());
            record.setPriceUpdateTime(stock.getPriceUpdateTime());
            record.setCustomerId(order.getCustomerId());
            record.setCustomerName(order.getCustomerName());
            record.setDeptId(order.getDeptId());
            record.setDeptCode(order.getDeptCode());
            record.setOutboundMode(order.getOutboundMode());
            record.setRequesterId(order.getRequesterId());
            record.setRequesterName(order.getRequesterName());
            record.setOperatorId(order.getOperatorId());
            record.setOperatorName(order.getOperatorName());
            record.setRemark(approveRemark);
            record.setBizDate(order.getBizDate());
            if (!stockRecordService.save(record)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫履歴保存に失敗しました");
            }

            if (Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())) {
                stockBatchService.recordInbound(order, item, stock);
                notifyInbound(item.getSkuCode(), changeQty, afterQty, order.getId());
            } else {
                notifyLowStock(item.getSkuCode(), afterQty, order.getId());
            }
        }

        order.setState(StockBizConstant.ORDER_STATE_FINISHED);
        order.setFinishTime(LocalDateTime.now());
        if (!stockOrderService.updateById(order)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "伝票更新に失敗しました");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitOrder(StockOrderSubmitDTO dto) {
        int orderType = dto.getOrderType() == null ? StockBizConstant.ORDER_TYPE_INBOUND : dto.getOrderType();
        if (orderType != StockBizConstant.ORDER_TYPE_INBOUND && orderType != StockBizConstant.ORDER_TYPE_OUTBOUND) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "伝票種別が不正です");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "明細は必須です");
        }

        List<OrderWorkingItem> workingItems = new ArrayList<>();
        int totalQty = 0;
        Long warehouseId = null;
        Long stockTypeId = null;
        String orderRemark = dto.getRemark();
        LocalDate bizDate = LocalDate.now();

        for (StockOrderSubmitItemDTO itemDTO : dto.getItems()) {
            Stock stock = requireStock(itemDTO.getStockId());
            if (orderType == StockBizConstant.ORDER_TYPE_OUTBOUND) {
                requireAccessibleOutboundStock(stock, StockBizConstant.OUTBOUND_MODE_CUSTOMER);
            }
            if (warehouseId == null) {
                warehouseId = Long.valueOf(stock.getWarehouseId());
            } else if (!warehouseId.equals(Long.valueOf(stock.getWarehouseId()))) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "同一伝票の倉庫は一致する必要があります");
            }
            if (stockTypeId == null) {
                stockTypeId = stock.getStockTypeId();
            }

            Goods goods = requireGoods(stock.getGoodsId());
            GoodsSku sku = requireSku(stock.getSkuId(), goods.getId());
            String stockTypeName = getStockTypeName(stock.getStockTypeId());

            int qty = safeInt(itemDTO.getQuantity());
            int beforeQty = safeInt(stock.getCurrentQty());
            int afterQty;
            if (orderType == StockBizConstant.ORDER_TYPE_INBOUND) {
                afterQty = beforeQty + qty;
            } else {
                if (beforeQty < qty) {
                    notifyInsufficientStock(sku.getSkuCode(), qty, beforeQty, stock.getId());
                    throw new co.handk.backend.exception.BusinessException(
                            co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                            "在庫数量が不足しています: SKU[" + sku.getSkuCode() + "]");
                }
                afterQty = beforeQty - qty;
            }

            OrderWorkingItem working = new OrderWorkingItem();
            working.stock = stock;
            working.goods = goods;
            working.sku = sku;
            working.stockTypeName = stockTypeName;
            working.changeQty = qty;
            working.beforeQty = beforeQty;
            working.afterQty = afterQty;
            working.remark = itemDTO.getRemark();
            workingItems.add(working);
            totalQty += qty;
        }

        boolean selfInbound = orderType == StockBizConstant.ORDER_TYPE_INBOUND
                && resolveInboundScene(dto.getSourceType()) == StockBizConstant.INBOUND_SCENE_SELF;
        int sourceType = selfInbound ? StockBizConstant.SOURCE_TYPE_REQUEST : StockBizConstant.SOURCE_TYPE_MANUAL;
        boolean needApprove = orderType == StockBizConstant.ORDER_TYPE_OUTBOUND || selfInbound;
        StockOrder order = new StockOrder();
        order.setOrderNo(generateOrderNo(orderType));
        order.setOrderType(orderType);
        order.setWarehouseId(warehouseId);
        order.setSourceType(sourceType);
        order.setTotalQty(totalQty);
        order.setStockTypeId(stockTypeId);
        order.setState(needApprove ? StockBizConstant.ORDER_STATE_APPROVING : StockBizConstant.ORDER_STATE_FINISHED);
        Long userId = UserContext.getUserIdOrDefault();
        User user = userService.getByIdNotDeleted(userId);
        String username = user == null ? null : user.getUsername();
        order.setRequesterId(userId);
        order.setRequesterName(username);
        order.setOperatorId(userId);
        order.setOperatorName(username);
        order.setRemark(orderRemark);
        order.setBizDate(bizDate);
        if (!needApprove) {
            order.setFinishTime(LocalDateTime.now());
        }
        if (!stockOrderService.save(order)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫伝票の保存に失敗しました");
        }

        for (OrderWorkingItem working : workingItems) {
            StockOrderItem item = new StockOrderItem();
            item.setOrderId(order.getId());
            item.setGoodsId(working.goods.getId());
            item.setSkuId(working.sku.getId());
            item.setSkuCode(working.sku.getSkuCode());
            item.setGoodsName(working.goods.getName());
            item.setEnglishName(working.goods.getEnglishName());
            item.setBrandId(working.goods.getBrandId());
            item.setSeriesId(working.goods.getSeriesId());
            item.setCategoryId(working.goods.getCategoryId());
            item.setMakerId(working.goods.getMakerId());
            item.setStockTypeId(working.stock.getStockTypeId());
            item.setStockTypeName(working.stockTypeName);
            item.setBeforeQty(working.beforeQty);
            item.setChangeQty(working.changeQty);
            item.setAfterQty(working.afterQty);
            item.setPrice(working.stock.getPrice());
            item.setCurrency(working.stock.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : working.stock.getCurrency());
            item.setRemark(working.remark == null ? orderRemark : working.remark);
            item.setBizDate(bizDate);
            if (!stockOrderItemService.save(item)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫明細の保存に失敗しました");
            }

            if (needApprove) {
                continue;
            }
            updateStockQuantityWithVersion(working.stock, working.afterQty);

            StockRecord record = new StockRecord();
            record.setBizNo(order.getOrderNo());
            record.setOrderId(order.getId());
            record.setOrderItemId(item.getId());
            record.setStockId(working.stock.getId());
            record.setGoodsId(item.getGoodsId());
            record.setSkuId(item.getSkuId());
            record.setSkuCode(item.getSkuCode());
            record.setGoodsName(item.getGoodsName());
            record.setEnglishName(item.getEnglishName());
            record.setBrandId(item.getBrandId());
            record.setBrandName(item.getBrandName());
            record.setSeriesId(item.getSeriesId());
            record.setSeriesName(item.getSeriesName());
            record.setCategoryId(item.getCategoryId());
            record.setCategoryName(item.getCategoryName());
            record.setStockTypeId(item.getStockTypeId());
            record.setStockTypeName(item.getStockTypeName());
            record.setMakerId(item.getMakerId());
            record.setMakerName(item.getMakerName());
            record.setWarehouseId(order.getWarehouseId());
            record.setBeforeQty(working.beforeQty);
            record.setChangeQty(working.changeQty);
            record.setAfterQty(working.afterQty);
            record.setOrderType(order.getOrderType());
            record.setSourceType(order.getSourceType());
            record.setPrice(item.getPrice());
            record.setCurrency(item.getCurrency());
            record.setPriceUpdateTime(working.stock.getPriceUpdateTime());
            record.setRequesterId(order.getRequesterId());
            record.setRequesterName(order.getRequesterName());
            record.setOperatorId(order.getOperatorId());
            record.setOperatorName(order.getOperatorName());
            record.setRemark(item.getRemark());
            record.setBizDate(order.getBizDate());
            if (!stockRecordService.save(record)) {
                throw new co.handk.backend.exception.BusinessException(
                        co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫履歴の保存に失敗しました");
            }

            if (orderType == StockBizConstant.ORDER_TYPE_INBOUND) {
                notifyInbound(item.getSkuCode(), working.changeQty, working.afterQty, order.getId());
            } else {
                notifyLowStock(item.getSkuCode(), working.afterQty, order.getId());
            }
        }
        return order.getId();
    }

    private Stock requireStock(Long stockId) {
        Stock stock = this.getByIdNotDeleted(stockId);
        if (stock == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        return stock;
    }

    private Stock requireStock(Long stockId, Integer warehouseId) {
        if (warehouseId == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "warehouseId is required for stock operation");
        }
        Stock stock = requireStock(stockId);
        if (stock.getWarehouseId() == null || !warehouseId.equals(stock.getWarehouseId())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "stock does not belong to requested warehouse");
        }
        return stock;
    }

    private Stock resolveInboundStock(StockOperateDTO dto) {
        if (dto.getStockId() != null) {
            return requireStock(dto.getStockId(), dto.getWarehouseId());
        }
        if (dto.getGoodsId() == null || dto.getSkuId() == null || dto.getWarehouseId() == null
                || dto.getStockTypeId() == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "goodsId, skuId, warehouseId and stockTypeId are required for inbound");
        }
        Goods goods = requireGoods(dto.getGoodsId());
        GoodsSku sku = requireSku(dto.getSkuId(), goods.getId());
        Stock existing = findStock(goods.getId(), sku.getId(), Long.valueOf(dto.getWarehouseId()), dto.getStockTypeId());
        if (existing != null) {
            return existing;
        }

        Stock stock = new Stock();
        stock.setGoodsId(dto.getGoodsId());
        stock.setGoodsName(goods.getName());
        stock.setSkuId(sku.getId());
        stock.setSkuCode(sku.getSkuCode());
        stock.setWarehouseId(dto.getWarehouseId());
        stock.setCurrentQty(0);
        stock.setLockQty(0);
        stock.setPrice(sku.getPrice());
        stock.setCurrency(sku.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : sku.getCurrency());
        stock.setPriceUpdateTime(sku.getPriceUpdateTime());
        stock.setStockTypeId(dto.getStockTypeId());
        stock.setStatus(StatusEnum.NOMAL.getCode());
        stock.setVersion(0L);
        if (!this.save(stock)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "failed to initialize stock for inbound");
        }
        return stock;
    }

    private Stock resolveOutboundStock(StockOperateDTO dto) {
        if (dto.getStockId() != null) {
            return requireStock(dto.getStockId(), dto.getWarehouseId());
        }
        if (dto.getGoodsId() == null || dto.getSkuId() == null || dto.getWarehouseId() == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "goodsId, skuId and warehouseId are required for outbound");
        }
        Long warehouseId = Long.valueOf(dto.getWarehouseId());
        Stock stock = findStock(Long.valueOf(dto.getGoodsId()), dto.getSkuId(), warehouseId, dto.getStockTypeId());
        if (stock == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "stock not found for outbound");
        }
        return stock;
    }

    private Goods requireGoods(Integer goodsId) {
        Goods goods = goodsService.getByIdNotDeleted(Long.valueOf(goodsId));
        if (goods == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        return goods;
    }

    private GoodsSku requireSku(Long skuId, Long goodsId) {
        GoodsSku sku = goodsSkuService.getByIdNotDeleted(skuId);
        if (sku == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        if (!goodsId.equals(sku.getGoodsId())) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        return sku;
    }

    private String getStockTypeName(Long stockTypeId) {
        if (stockTypeId == null) {
            return null;
        }
        StockType stockType = stockTypeService.getByIdNotDeleted(stockTypeId);
        return stockType == null ? null : stockType.getName();
    }

    private Stock findStock(Long goodsId, Long skuId, Long warehouseId, Long stockTypeId) {
        QueryWrapper<Stock> wrapper = new QueryWrapper<Stock>()
                .eq("goods_id", goodsId)
                .eq("sku_id", skuId)
                .eq("warehouse_id", warehouseId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode());
        if (stockTypeId == null) {
            wrapper.isNull("stock_type_id");
        } else {
            wrapper.eq("stock_type_id", stockTypeId);
        }
        return this.getOne(wrapper);
    }

    private StockOrder saveStockOrder(Stock stock, StockOperateDTO dto, int orderType, int sourceType, int state) {
        StockOrder order = new StockOrder();
        order.setOrderNo(generateOrderNo(orderType));
        order.setOrderType(orderType);
        order.setWarehouseId(Long.valueOf(stock.getWarehouseId()));
        order.setSourceType(sourceType);
        order.setTotalQty(dto.getQuantity());
        order.setStockTypeId(stock.getStockTypeId());
        order.setState(state);

        Long userId = UserContext.getUserIdOrDefault();
        User user = userService.getByIdNotDeleted(userId);
        order.setRequesterId(userId);
        order.setRequesterName(user == null ? null : user.getUsername());
        order.setOperatorId(userId);
        order.setOperatorName(user == null ? null : user.getUsername());
        order.setRemark(dto.getRemark());
        order.setOutboundMode(normalizeOutboundMode(dto.getOutboundMode()));
        order.setCustomerId(dto.getCustomerId());
        if (dto.getCustomerId() != null) {
            Customer customer = customerService.getByIdNotDeleted(dto.getCustomerId());
            order.setCustomerName(customer == null ? dto.getCustomerName() : customer.getName());
        } else {
            order.setCustomerName(dto.getCustomerName());
        }
        Long targetDeptId = dto.getDeptId();
        if (StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER.equals(order.getOutboundMode()) && targetDeptId == null) {
            targetDeptId = user == null ? null : user.getDeptId();
        }
        order.setDeptId(targetDeptId);
        order.setSaleDeadline(dto.getSaleDeadline());
        if (targetDeptId != null) {
            Dept dept = deptService.getByIdNotDeleted(targetDeptId);
            order.setDeptCode(dept == null ? null : dept.getCode());
        }
        order.setBizDate(LocalDate.now());
        if (state == StockBizConstant.ORDER_STATE_FINISHED) {
            order.setFinishTime(LocalDateTime.now());
        }
        if (!stockOrderService.save(order)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        return order;
    }

    private String normalizeOutboundMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return StockBizConstant.OUTBOUND_MODE_CUSTOMER;
        }
        if ("dept".equalsIgnoreCase(mode) || "group_allocate".equalsIgnoreCase(mode)) {
            return StockBizConstant.OUTBOUND_MODE_GROUP_ALLOCATE;
        }
        if ("group_customer".equalsIgnoreCase(mode)) {
            return StockBizConstant.OUTBOUND_MODE_GROUP_CUSTOMER;
        }
        return StockBizConstant.OUTBOUND_MODE_CUSTOMER;
    }

    private StockOrderItem saveOrderItem(Long orderId,
                                         Goods goods,
                                         GoodsSku sku,
                                         Stock stock,
                                         String stockTypeName,
                                         StockOperateDTO dto,
                                         int beforeQty,
                                         int afterQty) {
        StockOrderItem item = new StockOrderItem();
        item.setOrderId(orderId);
        item.setGoodsId(goods.getId());
        item.setSkuId(sku.getId());
        item.setSkuCode(sku.getSkuCode());
        item.setGoodsName(goods.getName());
        item.setEnglishName(goods.getEnglishName());
        item.setBrandId(goods.getBrandId());
        item.setSeriesId(goods.getSeriesId());
        item.setCategoryId(goods.getCategoryId());
        item.setMakerId(goods.getMakerId());
        item.setStockTypeId(stock.getStockTypeId());
        item.setStockTypeName(stockTypeName);
        item.setBeforeQty(beforeQty);
        item.setChangeQty(dto.getQuantity());
        item.setAfterQty(afterQty);
        item.setPrice(stock.getPrice());
        item.setCurrency(stock.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : stock.getCurrency());
        item.setRemark(dto.getRemark());
        item.setBizDate(LocalDate.now());
        if (!stockOrderItemService.save(item)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫明細の保存に失敗しました");
        }
        return item;
    }

    private void saveStockRecord(StockOrder order, Stock stock, String remark, int beforeQty, int afterQty) {
        StockOrderItem item = stockOrderItemService.getOne(new QueryWrapper<StockOrderItem>()
                .eq("order_id", order.getId())
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (item == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫明細が存在しません");
        }
        StockRecord record = new StockRecord();
        record.setBizNo(order.getOrderNo());
        record.setOrderId(order.getId());
        record.setOrderItemId(item.getId());
        record.setStockId(stock.getId());
        record.setGoodsId(item.getGoodsId());
        record.setSkuId(item.getSkuId());
        record.setSkuCode(item.getSkuCode());
        record.setGoodsName(item.getGoodsName());
        record.setEnglishName(item.getEnglishName());
        record.setBrandId(item.getBrandId());
        record.setBrandName(item.getBrandName());
        record.setSeriesId(item.getSeriesId());
        record.setSeriesName(item.getSeriesName());
        record.setCategoryId(item.getCategoryId());
        record.setCategoryName(item.getCategoryName());
        record.setStockTypeId(item.getStockTypeId());
        record.setStockTypeName(item.getStockTypeName());
        record.setMakerId(item.getMakerId());
        record.setMakerName(item.getMakerName());
        record.setWarehouseId(Long.valueOf(stock.getWarehouseId()));
        record.setBeforeQty(beforeQty);
        record.setChangeQty(item.getChangeQty());
        record.setAfterQty(afterQty);
        record.setOrderType(order.getOrderType());
        record.setSourceType(order.getSourceType());
        record.setPrice(item.getPrice());
        record.setCurrency(item.getCurrency());
        record.setPriceUpdateTime(stock.getPriceUpdateTime());
        record.setRequesterId(order.getRequesterId());
        record.setRequesterName(order.getRequesterName());
        record.setOperatorId(order.getOperatorId());
        record.setOperatorName(order.getOperatorName());
        record.setRemark(remark);
        record.setBizDate(order.getBizDate());
        if (!stockRecordService.save(record)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫履歴保存に失敗しました");
        }
    }

    private String generateOrderNo(int orderType) {
        String prefix = orderType == StockBizConstant.ORDER_TYPE_INBOUND ? "IN" : "OUT";
        return prefix + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int resolveInboundScene(Integer sourceType) {
        int scene = sourceType == null ? StockBizConstant.INBOUND_SCENE_RESALE : sourceType;
        if (scene != StockBizConstant.INBOUND_SCENE_SELF && scene != StockBizConstant.INBOUND_SCENE_RESALE) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "invalid inbound source type");
        }
        return scene;
    }

    private void updateStockQuantityWithVersion(Stock stock, int afterQty) {
        if (stock == null || stock.getId() == null) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        long oldVersion = stock.getVersion() == null ? 0L : stock.getVersion();
        boolean updated = this.update(new LambdaUpdateWrapper<Stock>()
                .eq(Stock::getId, stock.getId())
                .eq(Stock::getWarehouseId, stock.getWarehouseId())
                .eq(Stock::getDeleted, DeleteEnum.UNDELETED.getCode())
                .eq(Stock::getVersion, oldVersion)
                .set(Stock::getCurrentQty, afterQty)
                .set(Stock::getVersion, oldVersion + 1));
        if (!updated) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "処理に失敗しました");
        }
        stock.setCurrentQty(afterQty);
        stock.setVersion(oldVersion + 1);
    }

    private static class OrderWorkingItem {
        private Stock stock;
        private Goods goods;
        private GoodsSku sku;
        private String stockTypeName;
        private int changeQty;
        private int beforeQty;
        private int afterQty;
        private String remark;
    }

    private void notifyInbound(String skuCode, int qty, int afterQty, Long sourceId) {
        String text = String.format("入庫完了: SKU[%s] 数量=%d, 在庫残=%d", skuCode, qty, afterQty);
        saveMessage(MESSAGE_TYPE_INBOUND, text, sourceId);
    }

    private void notifyInsufficientStock(String skuCode, int requestQty, int currentQty, Long sourceId) {
        String text = String.format("在庫不足: SKU[%s] 要求=%d, 現在庫=%d", skuCode, requestQty, currentQty);
        saveMessage(MESSAGE_TYPE_WARNING, text, sourceId);
    }

    private void requireOutboundIdempotency(StockOperateDTO dto, Stock stock) {
        Long userId = UserContext.getUserIdOrDefault();
        String key = RedisKey.IDEMPOTENCY_REQUEST
                + "stock:outbound:"
                + userId + ":"
                + stock.getId() + ":"
                + stock.getGoodsId() + ":"
                + stock.getSkuId() + ":"
                + stock.getWarehouseId() + ":"
                + (stock.getStockTypeId() == null ? "null" : stock.getStockTypeId()) + ":"
                + dto.getQuantity() + ":"
                + (dto.getDeptId() == null ? "null" : dto.getDeptId()) + ":"
                + (dto.getCustomerId() == null ? "null" : dto.getCustomerId()) + ":"
                + normalizeOutboundMode(dto.getOutboundMode()) + ":"
                + (dto.getSaleDeadline() == null ? "null" : dto.getSaleDeadline());
        Boolean accepted = stringRedisUtil.setIfAbsent(key, "1", 300L, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(accepted)) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "duplicate outbound request detected, please retry later");
        }
    }

    private void notifyLowStock(String skuCode, int afterQty, Long sourceId) {
        if (afterQty > LOW_STOCK_THRESHOLD) {
            return;
        }
        String text = String.format("低在庫警告: SKU[%s] 在庫残=%d (閾値=%d)", skuCode, afterQty, LOW_STOCK_THRESHOLD);
        saveMessage(MESSAGE_TYPE_WARNING, text, sourceId);
    }

    private void saveMessage(int type, String messageText, Long sourceId) {
        Message message = new Message();
        message.setType(type);
        message.setUserId(UserContext.getUserIdOrDefault());
        message.setMessage(messageText);
        message.setSourceId(sourceId == null ? 0 : sourceId.intValue());
        message.setIsRead(MESSAGE_IS_UNREAD);
        message.setState(MESSAGE_STATE_SENT);
        messageService.save(message);
    }

    @Override
    protected <D> Stock toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Stock entity = new Stock();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    protected StockVO toVO(Stock entity) {
        if (entity == null) {
            return null;
        }
        StockVO vo = new StockVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStockTypeName(getStockTypeName(entity.getStockTypeId()));
        java.util.Map<String, Integer> groupQty = stockBatchService.getGroupQuantities(entity.getId());
        vo.setGroupAQty(groupQty.getOrDefault("A", 0));
        vo.setGroupBQty(groupQty.getOrDefault("B", 0));
        vo.setGroupCQty(groupQty.getOrDefault("C", 0));
        return vo;
    }
}
