package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.entity.Message;
import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.entity.StockRecord;
import co.handk.backend.entity.StockType;
import co.handk.backend.entity.User;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.MessageService;
import co.handk.backend.service.StockOrderItemService;
import co.handk.backend.service.StockOrderService;
import co.handk.backend.service.StockRecordService;
import co.handk.backend.service.StockService;
import co.handk.backend.service.StockTypeService;
import co.handk.backend.service.UserService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.create.StockOperateDTO;
import co.handk.common.model.vo.StockVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class StockServiceImpl extends BaseServiceImpl<StockMapper, Stock, StockVO> implements StockService {

    private static final int MESSAGE_TYPE_INBOUND = 1;
    private static final int MESSAGE_TYPE_WARNING = 2;
    private static final int MESSAGE_IS_UNREAD = 0;
    private static final int MESSAGE_STATE_SENT = 1;
    private static final int LOW_STOCK_THRESHOLD = 10;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long inbound(StockOperateDTO dto) {
        Stock stock = requireStock(dto.getStockId());
        Goods goods = requireGoods(stock.getGoodsId());
        GoodsSku sku = requireSku(stock.getSkuId(), goods.getId());
        String stockTypeName = getStockTypeName(stock.getStockTypeId());

        int scene = dto.getSourceType() == null ? StockBizConstant.INBOUND_SCENE_RESALE : dto.getSourceType();
        boolean needApprove = scene == StockBizConstant.INBOUND_SCENE_SELF;

        int beforeQty = safeInt(stock.getCurrentQty());
        int afterQty = beforeQty + dto.getQuantity();

        int state = needApprove ? StockBizConstant.ORDER_STATE_APPROVING : StockBizConstant.ORDER_STATE_FINISHED;
        int sourceType = needApprove ? StockBizConstant.SOURCE_TYPE_REQUEST : StockBizConstant.SOURCE_TYPE_MANUAL;

        StockOrder order = saveStockOrder(stock, dto, StockBizConstant.ORDER_TYPE_INBOUND, sourceType, state);
        saveOrderItem(order.getId(), goods, sku, stock, stockTypeName, dto, beforeQty, afterQty);

        if (!needApprove) {
            stock.setCurrentQty(afterQty);
            if (!this.updateById(stock)) {
                throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫の更新に失敗しました");
            }
            saveStockRecord(order, stock, dto.getRemark(), beforeQty, afterQty);
            notifyInbound(sku.getSkuCode(), dto.getQuantity(), afterQty, order.getId());
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long outbound(StockOperateDTO dto) {
        Stock stock = requireStock(dto.getStockId());
        Goods goods = requireGoods(stock.getGoodsId());
        GoodsSku sku = requireSku(stock.getSkuId(), goods.getId());
        String stockTypeName = getStockTypeName(stock.getStockTypeId());

        int beforeQty = safeInt(stock.getCurrentQty());
        if (beforeQty < dto.getQuantity()) {
            notifyInsufficientStock(sku.getSkuCode(), dto.getQuantity(), beforeQty, stock.getId());
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫数が不足しています");
        }
        int afterQty = beforeQty - dto.getQuantity();

        StockOrder order = saveStockOrder(stock, dto, StockBizConstant.ORDER_TYPE_OUTBOUND,
                StockBizConstant.SOURCE_TYPE_MANUAL, StockBizConstant.ORDER_STATE_FINISHED);
        saveOrderItem(order.getId(), goods, sku, stock, stockTypeName, dto, beforeQty, afterQty);

        stock.setCurrentQty(afterQty);
        if (!this.updateById(stock)) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫の更新に失敗しました");
        }
        saveStockRecord(order, stock, dto.getRemark(), beforeQty, afterQty);
        notifyLowStock(sku.getSkuCode(), afterQty, order.getId());
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveInbound(Long orderId, Boolean approved, String approveRemark) {
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        if (order == null) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票が存在しません");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票ではありません");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_STATE_APPROVING).equals(order.getState())) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "承認待ち状態の伝票ではありません");
        }

        order.setApproverId(UserContext.getUserIdOrDefault());
        User approver = userService.getByIdNotDeleted(order.getApproverId());
        order.setApproverName(approver == null ? null : approver.getUsername());
        order.setApproveTime(LocalDateTime.now());
        order.setRemark(approveRemark);

        if (Boolean.FALSE.equals(approved)) {
            order.setState(StockBizConstant.ORDER_STATE_CANCELED);
            if (!stockOrderService.updateById(order)) {
                throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票の状態更新に失敗しました");
            }
            return true;
        }

        List<StockOrderItem> items = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", orderId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (items == null || items.isEmpty()) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票明細が存在しません");
        }

        for (StockOrderItem item : items) {
            Stock stock = findStock(item.getGoodsId(), item.getSkuId(), order.getWarehouseId(), item.getStockTypeId());
            if (stock == null) {
                throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "承認対象の在庫商品が存在しません");
            }

            int beforeQty = safeInt(stock.getCurrentQty());
            int afterQty = beforeQty + safeInt(item.getChangeQty());
            stock.setCurrentQty(afterQty);
            if (!this.updateById(stock)) {
                throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫の更新に失敗しました");
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
            record.setRequesterId(order.getRequesterId());
            record.setRequesterName(order.getRequesterName());
            record.setOperatorId(order.getOperatorId());
            record.setOperatorName(order.getOperatorName());
            record.setRemark(approveRemark);
            if (!stockRecordService.save(record)) {
                throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫履歴の保存に失敗しました");
            }

            notifyInbound(item.getSkuCode(), safeInt(item.getChangeQty()), afterQty, order.getId());
        }

        order.setState(StockBizConstant.ORDER_STATE_FINISHED);
        order.setFinishTime(LocalDateTime.now());
        if (!stockOrderService.updateById(order)) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入庫伝票の状態更新に失敗しました");
        }
        return true;
    }

    private Stock requireStock(Long stockId) {
        Stock stock = this.getByIdNotDeleted(stockId);
        if (stock == null) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫商品が存在しません");
        }
        return stock;
    }

    private Goods requireGoods(Integer goodsId) {
        Goods goods = goodsService.getByIdNotDeleted(Long.valueOf(goodsId));
        if (goods == null) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "商品が存在しません");
        }
        return goods;
    }

    private GoodsSku requireSku(Long skuId, Long goodsId) {
        GoodsSku sku = goodsSkuService.getByIdNotDeleted(skuId);
        if (sku == null) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "SKUが存在しません");
        }
        if (!goodsId.equals(sku.getGoodsId())) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "SKUと商品の関連が不正です");
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
        if (state == StockBizConstant.ORDER_STATE_FINISHED) {
            order.setFinishTime(LocalDateTime.now());
        }
        if (!stockOrderService.save(order)) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫伝票の保存に失敗しました");
        }
        return order;
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
        if (!stockOrderItemService.save(item)) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫明細の保存に失敗しました");
        }
        return item;
    }

    private void saveStockRecord(StockOrder order, Stock stock, String remark, int beforeQty, int afterQty) {
        StockOrderItem item = stockOrderItemService.getOne(new QueryWrapper<StockOrderItem>()
                .eq("order_id", order.getId())
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (item == null) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "入出庫明細が存在しません");
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
        if (!stockRecordService.save(record)) {
            throw new co.handk.backend.exception.BusinessException(co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME, "在庫履歴の保存に失敗しました");
        }
    }

    private String generateOrderNo(int orderType) {
        String prefix = orderType == StockBizConstant.ORDER_TYPE_INBOUND ? "IN" : "OUT";
        return prefix + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void notifyInbound(String skuCode, int qty, int afterQty, Long sourceId) {
        String text = String.format("入庫完了: SKU[%s] 数量=%d, 在庫残=%d", skuCode, qty, afterQty);
        saveMessage(MESSAGE_TYPE_INBOUND, text, sourceId);
    }

    private void notifyInsufficientStock(String skuCode, int requestQty, int currentQty, Long sourceId) {
        String text = String.format("在庫不足: SKU[%s] 要求=%d, 現在在庫=%d", skuCode, requestQty, currentQty);
        saveMessage(MESSAGE_TYPE_WARNING, text, sourceId);
    }

    private void notifyLowStock(String skuCode, int afterQty, Long sourceId) {
        if (afterQty > LOW_STOCK_THRESHOLD) {
            return;
        }
        String text = String.format("低在庫警告: SKU[%s] 在庫残=%d (しきい値=%d)", skuCode, afterQty, LOW_STOCK_THRESHOLD);
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
        return vo;
    }
}