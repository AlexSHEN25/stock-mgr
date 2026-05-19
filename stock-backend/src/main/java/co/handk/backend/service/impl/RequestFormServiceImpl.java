package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.Customer;
import co.handk.backend.entity.Dept;
import co.handk.backend.entity.RequestForm;
import co.handk.backend.entity.RequestItem;
import co.handk.backend.entity.Stock;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.entity.User;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.mapper.RequestFormMapper;
import co.handk.backend.service.DeptService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.RequestFormService;
import co.handk.backend.service.RequestItemService;
import co.handk.backend.service.StockOrderItemService;
import co.handk.backend.service.StockOrderService;
import co.handk.backend.service.CustomerService;
import co.handk.backend.service.UserService;
import co.handk.common.constant.CommonConstant;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.create.CreateRequestFromOutboundDTO;
import co.handk.common.model.vo.RequestFormVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RequestFormServiceImpl extends BaseServiceImpl<RequestFormMapper, RequestForm, RequestFormVO>
        implements RequestFormService {

    @Autowired
    private StockOrderService stockOrderService;
    @Autowired
    private StockOrderItemService stockOrderItemService;
    @Autowired
    private RequestItemService requestItemService;
    @Autowired
    private UserService userService;
    @Autowired
    private DeptService deptService;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private PermissionQueryService permissionQueryService;
    @Autowired
    private CustomerService customerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof co.handk.common.model.dto.create.CreateRequestFormDTO createDto) {
            Long userId = UserContext.getUserIdOrDefault();
            if (!permissionQueryService.isSuperAdmin(userId)) {
                createDto.setUserId(userId);
                User user = userService.getByIdNotDeleted(userId);
                if (user != null) {
                    createDto.setUsername(user.getUsername());
                    createDto.setDeptId(user.getDeptId());
                    Dept dept = user.getDeptId() == null ? null : deptService.getByIdNotDeleted(user.getDeptId());
                    createDto.setDeptName(dept == null ? null : dept.getName());
                }
            }
            validateCustomerOwnership(createDto.getCustomerId(), userId);
        }
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (dto instanceof co.handk.common.model.dto.update.UpdateRequestFormDTO updateDto) {
            RequestForm existed = super.getByIdNotDeleted(updateDto.getId());
            requireOwned(existed);
            Long userId = UserContext.getUserIdOrDefault();
            if (!permissionQueryService.isSuperAdmin(userId)) {
                updateDto.setUserId(existed.getUserId());
                updateDto.setUsername(existed.getUsername());
                updateDto.setDeptId(existed.getDeptId());
                updateDto.setDeptName(existed.getDeptName());
            }
            validateCustomerOwnership(updateDto.getCustomerId(), userId);
        }
        return super.updateByDto(dto);
    }

    @Override
    protected <Q> QueryWrapper<RequestForm> buildWrapper(Q dto) {
        QueryWrapper<RequestForm> wrapper = super.buildWrapper(dto);
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.eq("user_id", userId);
        }
        return wrapper;
    }

    @Override
    public RequestForm getByIdNotDeleted(java.io.Serializable id) {
        RequestForm form = super.getByIdNotDeleted(id);
        requireOwned(form);
        return form;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        RequestForm form = super.getByIdNotDeleted(id);
        requireOwned(form);
        return super.deleteByIdLogic(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchLogic(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return DeleteEnum.UNDELETED.getCode();
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return super.deleteBatchLogic(ids);
        }
        int rows = 0;
        for (Long id : ids) {
            RequestForm form = super.getByIdNotDeleted(id);
            requireOwned(form);
            rows += super.deleteByIdLogic(id);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromOutbound(CreateRequestFromOutboundDTO dto) {
        StockOrder outboundOrder = stockOrderService.getByIdNotDeleted(dto.getStockOrderId());
        if (outboundOrder == null) {
            throw new RuntimeException("出庫伝票が存在しません");
        }
        if (!Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(outboundOrder.getOrderType())) {
            throw new RuntimeException("出庫伝票のみ申請作成できます");
        }

        Long loginUserId = UserContext.getUserIdOrDefault();
        if (!loginUserId.equals(outboundOrder.getRequesterId()) && !loginUserId.equals(outboundOrder.getOperatorId())) {
            throw new RuntimeException("自分の出庫伝票のみ申請できます");
        }

        List<StockOrderItem> allItems = stockOrderItemService.list(new QueryWrapper<StockOrderItem>()
                .eq("order_id", outboundOrder.getId())
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (allItems.isEmpty()) {
            throw new RuntimeException("出庫明細が存在しません");
        }

        List<StockOrderItem> selectedItems = filterItems(allItems, dto.getStockOrderItemIds());
        if (selectedItems.isEmpty()) {
            throw new RuntimeException("申請対象の出庫明細がありません");
        }

        User user = userService.getByIdNotDeleted(loginUserId);
        Dept dept = user != null && user.getDeptId() != null ? deptService.getByIdNotDeleted(user.getDeptId()) : null;

        RequestForm form = new RequestForm();
        form.setBizNo(generateRequestNo());
        form.setUserId(loginUserId);
        form.setUsername(user == null ? null : user.getUsername());
        form.setDeptId(user == null ? null : user.getDeptId());
        form.setDeptName(dept == null ? null : dept.getName());
        form.setWarehouseId(outboundOrder.getWarehouseId());
        form.setState(StockBizConstant.REQUEST_STATE_CREATED);
        form.setApproveRemark(dto.getRemark());

        int totalQty = 0;
        BigDecimal totalAmt = BigDecimal.ZERO;

        if (!this.save(form)) {
            throw new RuntimeException("申請書の保存に失敗しました");
        }

        for (StockOrderItem orderItem : selectedItems) {
            RequestItem requestItem = new RequestItem();
            requestItem.setRequestId(form.getId());
            requestItem.setGoodsId(orderItem.getGoodsId());
            requestItem.setSkuId(orderItem.getSkuId());
            requestItem.setSkuCode(orderItem.getSkuCode());
            requestItem.setGoodsName(orderItem.getGoodsName());
            requestItem.setEnglishName(orderItem.getEnglishName());
            requestItem.setBrandId(orderItem.getBrandId());
            requestItem.setBrandName(orderItem.getBrandName());
            requestItem.setSeriesId(orderItem.getSeriesId());
            requestItem.setSeriesName(orderItem.getSeriesName());
            requestItem.setCategoryId(orderItem.getCategoryId());
            requestItem.setCategoryName(orderItem.getCategoryName());
            requestItem.setStockTypeId(orderItem.getStockTypeId());
            requestItem.setStockTypeName(orderItem.getStockTypeName());
            requestItem.setMakerId(orderItem.getMakerId());
            requestItem.setMakerName(orderItem.getMakerName());
            requestItem.setWarehouseId(outboundOrder.getWarehouseId());
            requestItem.setPrice(orderItem.getPrice());
            requestItem.setExchangeRate(BigDecimal.ONE);
            requestItem.setCurrency(orderItem.getCurrency() == null ? CommonConstant.DEFAULT_CURRENCY_JPY : orderItem.getCurrency());
            requestItem.setDiscount(BigDecimal.ONE);
            requestItem.setRequestQty(orderItem.getChangeQty());
            requestItem.setApproveQty(0);
            requestItem.setOutQty(orderItem.getChangeQty());
            requestItem.setTotalAmt(safeAmount(orderItem.getPrice()).multiply(BigDecimal.valueOf(orderItem.getChangeQty() == null ? 0 : orderItem.getChangeQty())));
            requestItem.setStockRecordId(null);
            requestItem.setRemark(dto.getRemark());
            if (!requestItemService.save(requestItem)) {
                throw new RuntimeException("申請明細の保存に失敗しました");
            }

            totalQty += orderItem.getChangeQty() == null ? 0 : orderItem.getChangeQty();
            totalAmt = totalAmt.add(safeAmount(requestItem.getTotalAmt()));
        }

        form.setTotalQty(totalQty);
        form.setRequestQty(totalQty);
        form.setTotalAmt(totalAmt);
        if (!this.updateById(form)) {
            throw new RuntimeException("申請書の集計更新に失敗しました");
        }
        return form.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reapplyInbound(Long requestId) {
        RequestForm form = this.getByIdNotDeleted(requestId);
        if (form == null) {
            throw new RuntimeException("申請書が存在しません");
        }

        List<RequestItem> items = requestItemService.list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (items.isEmpty()) {
            throw new RuntimeException("申請明細が存在しません");
        }

        Long loginUserId = UserContext.getUserIdOrDefault();

        StockOrder inboundOrder = new StockOrder();
        inboundOrder.setOrderNo(generateInboundNo());
        inboundOrder.setOrderType(StockBizConstant.ORDER_TYPE_INBOUND);
        inboundOrder.setWarehouseId(form.getWarehouseId());
        inboundOrder.setSourceType(StockBizConstant.SOURCE_TYPE_REQUEST);
        inboundOrder.setSourceId(form.getId());
        inboundOrder.setState(StockBizConstant.ORDER_STATE_APPROVING);
        inboundOrder.setRequesterId(loginUserId);
        inboundOrder.setRequesterName(form.getUsername());
        inboundOrder.setOperatorId(loginUserId);
        inboundOrder.setOperatorName(form.getUsername());
        inboundOrder.setRemark("申請書再入庫: " + form.getBizNo());

        int totalQty = 0;
        for (RequestItem item : items) {
            totalQty += item.getRequestQty() == null ? 0 : item.getRequestQty();
        }
        inboundOrder.setTotalQty(totalQty);
        inboundOrder.setStockTypeId(items.get(0).getStockTypeId());
        if (!stockOrderService.save(inboundOrder)) {
            throw new RuntimeException("再入庫注文の保存に失敗しました");
        }

        for (RequestItem reqItem : items) {
            Stock stock = findStock(reqItem.getGoodsId(), reqItem.getSkuId(), reqItem.getWarehouseId(), reqItem.getStockTypeId());
            if (stock == null) {
                throw new RuntimeException("在庫商品が存在しません。goodsId=" + reqItem.getGoodsId() + ", skuId=" + reqItem.getSkuId());
            }

            int beforeQty = stock.getCurrentQty() == null ? 0 : stock.getCurrentQty();
            int changeQty = reqItem.getRequestQty() == null ? 0 : reqItem.getRequestQty();
            int afterQty = beforeQty + changeQty;

            StockOrderItem orderItem = new StockOrderItem();
            orderItem.setOrderId(inboundOrder.getId());
            orderItem.setGoodsId(reqItem.getGoodsId());
            orderItem.setSkuId(reqItem.getSkuId());
            orderItem.setSkuCode(reqItem.getSkuCode());
            orderItem.setGoodsName(reqItem.getGoodsName());
            orderItem.setEnglishName(reqItem.getEnglishName());
            orderItem.setBrandId(reqItem.getBrandId());
            orderItem.setBrandName(reqItem.getBrandName());
            orderItem.setSeriesId(reqItem.getSeriesId());
            orderItem.setSeriesName(reqItem.getSeriesName());
            orderItem.setCategoryId(reqItem.getCategoryId());
            orderItem.setCategoryName(reqItem.getCategoryName());
            orderItem.setStockTypeId(reqItem.getStockTypeId());
            orderItem.setStockTypeName(reqItem.getStockTypeName());
            orderItem.setMakerId(reqItem.getMakerId());
            orderItem.setMakerName(reqItem.getMakerName());
            orderItem.setBeforeQty(beforeQty);
            orderItem.setChangeQty(changeQty);
            orderItem.setAfterQty(afterQty);
            orderItem.setPrice(reqItem.getPrice());
            orderItem.setCurrency(reqItem.getCurrency());
            orderItem.setRemark("申請書再入庫明細");
            if (!stockOrderItemService.save(orderItem)) {
                throw new RuntimeException("再入庫明細の保存に失敗しました");
            }
        }

        form.setState(StockBizConstant.REQUEST_STATE_REINBOUND_APPLIED);
        if (!this.updateById(form)) {
            throw new RuntimeException("申請書状態の更新に失敗しました");
        }
        return inboundOrder.getId();
    }

    private List<StockOrderItem> filterItems(List<StockOrderItem> allItems, List<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return allItems;
        }
        List<StockOrderItem> selected = new ArrayList<>();
        for (StockOrderItem item : allItems) {
            if (selectedIds.contains(item.getId())) {
                selected.add(item);
            }
        }
        return selected;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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
        return stockMapper.selectOne(wrapper);
    }

    private String generateRequestNo() {
        return "REQ" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private String generateInboundNo() {
        return "IN" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private void validateCustomerOwnership(Long customerId, Long userId) {
        if (customerId == null) {
            return;
        }
        if (permissionQueryService.isSuperAdmin(userId)) {
            return;
        }
        Customer customer = customerService.getByIdNotDeleted(customerId);
        if (customer == null || !userId.equals(customer.getOwnerUserId())) {
            throw new RuntimeException("自分名義の顧客のみ選択できます");
        }
    }

    private void requireOwned(RequestForm form) {
        if (form == null) {
            return;
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return;
        }
        if (!userId.equals(form.getUserId())) {
            throw new RuntimeException("この申請書データにアクセスする権限がありません");
        }
    }

    @Override
    protected RequestFormVO toVO(RequestForm entity) {
        if (entity == null) {
            return null;
        }
        RequestFormVO vo = new RequestFormVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> RequestForm toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        RequestForm entity = new RequestForm();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getId() == null) {
            entity.setBizNo(generateRequestNo());
        }
        return entity;
    }
}
