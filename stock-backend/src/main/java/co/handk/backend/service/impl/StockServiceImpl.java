package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.Goods;
import co.handk.backend.entity.GoodsSku;
import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.entity.StockRecord;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.service.GoodsService;
import co.handk.backend.service.GoodsSkuService;
import co.handk.backend.service.StockOrderItemService;
import co.handk.backend.service.StockOrderService;
import co.handk.backend.service.StockRecordService;
import co.handk.backend.service.StockService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
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

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private StockOrderService stockOrderService;
    @Autowired
    private StockOrderItemService stockOrderItemService;
    @Autowired
    private StockRecordService stockRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long inbound(StockOperateDTO dto) {
        Goods goods = requireGoods(dto.getGoodsId());
        GoodsSku sku = requireSku(dto.getSkuId(), dto.getGoodsId());

        int scene = dto.getSourceType() == null ? StockBizConstant.INBOUND_SCENE_RESALE : dto.getSourceType();
        boolean needApprove = scene == StockBizConstant.INBOUND_SCENE_SELF;

        Stock stock = findOrCreateStock(goods, sku, dto);
        int beforeQty = safeInt(stock.getCurrentQty());
        int afterQty = beforeQty + dto.getQuantity();

        int state = needApprove ? StockBizConstant.ORDER_STATE_APPROVING : StockBizConstant.ORDER_STATE_FINISHED;
        int sourceType = needApprove ? StockBizConstant.SOURCE_TYPE_REQUEST : StockBizConstant.SOURCE_TYPE_MANUAL;
        StockOrder order = saveStockOrder(dto, StockBizConstant.ORDER_TYPE_INBOUND, sourceType, state);
        StockOrderItem item = saveOrderItem(order.getId(), goods, sku, dto, beforeQty, afterQty);

        if (!needApprove) {
            stock.setCurrentQty(afterQty);
            this.updateById(stock);
            saveStockRecord(order, item, stock, dto, beforeQty, afterQty);
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long outbound(StockOperateDTO dto) {
        Goods goods = requireGoods(dto.getGoodsId());
        GoodsSku sku = requireSku(dto.getSkuId(), dto.getGoodsId());
        Stock stock = findStock(dto.getGoodsId(), dto.getSkuId(), dto.getWarehouseId(), dto.getStockTypeId());
        if (stock == null) {
            throw new RuntimeException("Stock not found");
        }

        int beforeQty = safeInt(stock.getCurrentQty());
        if (beforeQty < dto.getQuantity()) {
            throw new RuntimeException("Insufficient stock quantity");
        }
        int afterQty = beforeQty - dto.getQuantity();

        StockOrder order = saveStockOrder(
                dto,
                StockBizConstant.ORDER_TYPE_OUTBOUND,
                StockBizConstant.SOURCE_TYPE_MANUAL,
                StockBizConstant.ORDER_STATE_FINISHED
        );
        StockOrderItem item = saveOrderItem(order.getId(), goods, sku, dto, beforeQty, afterQty);

        stock.setCurrentQty(afterQty);
        this.updateById(stock);
        saveStockRecord(order, item, stock, dto, beforeQty, afterQty);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveInbound(Long orderId, Boolean approved, String approveRemark) {
        StockOrder order = stockOrderService.getByIdNotDeleted(orderId);
        if (order == null) {
            throw new RuntimeException("Inbound order not found");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(order.getOrderType())) {
            throw new RuntimeException("Order type is not inbound");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_STATE_APPROVING).equals(order.getState())) {
            throw new RuntimeException("Order is not in approving state");
        }

        Long approverId = UserContext.getUserIdOrDefault();
        order.setApproverId(approverId);
        order.setApproveTime(LocalDateTime.now());
        order.setRemark(approveRemark);

        if (Boolean.FALSE.equals(approved)) {
            order.setState(StockBizConstant.ORDER_STATE_CANCELED);
            stockOrderService.updateById(order);
            return true;
        }

        List<StockOrderItem> items = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", orderId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));

        for (StockOrderItem item : items) {
            Stock stock = findStock(
                    item.getGoodsId(),
                    item.getSkuId(),
                    order.getWarehouseId(),
                    item.getStockTypeId()
            );
            if (stock == null) {
                stock = new Stock();
                stock.setGoodsId(item.getGoodsId().intValue());
                stock.setGoodsName(item.getGoodsName());
                stock.setSkuId(item.getSkuId());
                stock.setSkuCode(item.getSkuCode());
                stock.setWarehouseId(order.getWarehouseId().intValue());
                stock.setStockTypeId(item.getStockTypeId());
                stock.setCurrentQty(0);
                stock.setLockQty(0);
                stock.setPrice(item.getPrice());
                stock.setCurrency(item.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : item.getCurrency());
                stock.setStatus(StatusEnum.NOMAL.getCode());
                this.save(stock);
            }

            int beforeQty = safeInt(stock.getCurrentQty());
            int afterQty = beforeQty + safeInt(item.getChangeQty());
            stock.setCurrentQty(afterQty);
            this.updateById(stock);

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
            record.setOperatorId(UserContext.getUserIdOrDefault());
            record.setRemark(approveRemark);
            stockRecordService.save(record);
        }

        order.setState(StockBizConstant.ORDER_STATE_FINISHED);
        order.setFinishTime(LocalDateTime.now());
        stockOrderService.updateById(order);
        return true;
    }

    private Goods requireGoods(Long goodsId) {
        Goods goods = goodsService.getByIdNotDeleted(goodsId);
        if (goods == null) {
            throw new RuntimeException("Goods not found");
        }
        return goods;
    }

    private GoodsSku requireSku(Long skuId, Long goodsId) {
        GoodsSku sku = goodsSkuService.getByIdNotDeleted(skuId);
        if (sku == null) {
            throw new RuntimeException("SKU not found");
        }
        if (!goodsId.equals(sku.getGoodsId())) {
            throw new RuntimeException("SKU does not belong to goods");
        }
        return sku;
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

    private Stock findOrCreateStock(Goods goods, GoodsSku sku, StockOperateDTO dto) {
        Stock stock = findStock(dto.getGoodsId(), dto.getSkuId(), dto.getWarehouseId(), dto.getStockTypeId());
        if (stock != null) {
            return stock;
        }

        Stock created = new Stock();
        created.setGoodsId(dto.getGoodsId().intValue());
        created.setGoodsName(goods.getName());
        created.setSkuId(dto.getSkuId());
        created.setSkuCode(sku.getSkuCode());
        created.setWarehouseId(dto.getWarehouseId().intValue());
        created.setCurrentQty(0);
        created.setLockQty(0);
        created.setPrice(sku.getPrice());
        created.setCurrency(sku.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : sku.getCurrency());
        created.setPriceUpdateTime(sku.getPriceUpdateTime());
        created.setStockTypeId(dto.getStockTypeId());
        created.setStatus(StatusEnum.NOMAL.getCode());
        this.save(created);
        return created;
    }

    private StockOrder saveStockOrder(StockOperateDTO dto, int orderType, int sourceType, int state) {
        StockOrder order = new StockOrder();
        order.setOrderNo(generateOrderNo(orderType));
        order.setOrderType(orderType);
        order.setWarehouseId(dto.getWarehouseId());
        order.setSourceType(sourceType);
        order.setTotalQty(dto.getQuantity());
        order.setStockTypeId(dto.getStockTypeId());
        order.setState(state);

        Long userId = UserContext.getUserIdOrDefault();
        order.setRequesterId(userId);
        order.setOperatorId(userId);
        order.setRemark(dto.getRemark());
        if (state == StockBizConstant.ORDER_STATE_FINISHED) {
            order.setFinishTime(LocalDateTime.now());
        }
        stockOrderService.save(order);
        return order;
    }

    private StockOrderItem saveOrderItem(Long orderId, Goods goods, GoodsSku sku, StockOperateDTO dto, int beforeQty, int afterQty) {
        StockOrderItem item = new StockOrderItem();
        item.setOrderId(orderId);
        item.setGoodsId(dto.getGoodsId());
        item.setSkuId(dto.getSkuId());
        item.setSkuCode(sku.getSkuCode());
        item.setGoodsName(goods.getName());
        item.setEnglishName(goods.getEnglishName());
        item.setBrandId(goods.getBrandId());
        item.setSeriesId(goods.getSeriesId());
        item.setCategoryId(goods.getCategoryId());
        item.setMakerId(goods.getMakerId());
        item.setStockTypeId(dto.getStockTypeId());
        item.setBeforeQty(beforeQty);
        item.setChangeQty(dto.getQuantity());
        item.setAfterQty(afterQty);
        item.setPrice(sku.getPrice());
        item.setCurrency(sku.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : sku.getCurrency());
        item.setRemark(dto.getRemark());
        stockOrderItemService.save(item);
        return item;
    }

    private void saveStockRecord(StockOrder order, StockOrderItem item, Stock stock, StockOperateDTO dto, int beforeQty, int afterQty) {
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
        record.setWarehouseId(dto.getWarehouseId());
        record.setBeforeQty(beforeQty);
        record.setChangeQty(dto.getQuantity());
        record.setAfterQty(afterQty);
        record.setOrderType(order.getOrderType());
        record.setSourceType(order.getSourceType());
        record.setPrice(item.getPrice());
        record.setCurrency(item.getCurrency());
        record.setPriceUpdateTime(stock.getPriceUpdateTime());
        record.setRequesterId(order.getRequesterId());
        record.setOperatorId(order.getOperatorId());
        record.setRemark(dto.getRemark());
        stockRecordService.save(record);
    }

    private String generateOrderNo(int orderType) {
        String prefix = orderType == StockBizConstant.ORDER_TYPE_INBOUND ? "IN" : "OUT";
        return prefix + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
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

