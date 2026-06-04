package co.handk.backend.service.impl;

import co.handk.backend.entity.RequestForm;
import co.handk.backend.entity.RequestItem;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.entity.StockRecord;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.RequestFormMapper;
import co.handk.backend.mapper.RequestItemMapper;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.RequestItemService;
import co.handk.backend.service.StockOrderItemService;
import co.handk.backend.service.StockOrderService;
import co.handk.backend.service.StockRecordService;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.vo.RequestItemVO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RequestItemServiceImpl extends BaseServiceImpl<RequestItemMapper, RequestItem, RequestItemVO>
        implements RequestItemService {

    private final StockRecordService stockRecordService;
    private final StockOrderItemService stockOrderItemService;
    private final StockOrderService stockOrderService;
    private final RequestFormMapper requestFormMapper;
    private final PermissionQueryService permissionQueryService;

    @Override
    protected RequestItemVO toVO(RequestItem entity) {
        if (entity == null) {
            return null;
        }
        RequestItemVO vo = new RequestItemVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> RequestItem toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        RequestItem entity = new RequestItem();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    protected <Q> QueryWrapper<RequestItem> buildWrapper(Q dto) {
        QueryWrapper<RequestItem> wrapper = super.buildWrapper(dto);
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.inSql("request_id",
                    "SELECT id FROM t_request_form WHERE deleted = 0 AND user_id = " + userId);
        }
        return wrapper;
    }

    @Override
    public RequestItem getByIdNotDeleted(Serializable id) {
        RequestItem item = super.getByIdNotDeleted(id);
        requireOwned(item);
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        RequestItem item = toEntity(dto);
        requireEditableOwnedRequest(item == null ? null : item.getRequestId());
        applyCalculatedTotal(item, null);
        boolean saved = save(item);
        if (saved) {
            recalculateRequestFormSummary(item.getRequestId());
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        RequestItem item = toEntity(dto);
        RequestItem existing = getByIdNotDeleted(item.getId());
        if (existing == null) {
            return false;
        }
        Long requestId = item.getRequestId() == null ? existing.getRequestId() : item.getRequestId();
        requireEditableOwnedRequest(requestId);
        applyCalculatedTotal(item, existing);
        boolean updated = super.updateByDto(item);
        if (updated) {
            recalculateRequestFormSummary(existing.getRequestId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        RequestItem item = getByIdNotDeleted(id);
        if (item == null) {
            return 0;
        }
        requireEditableOwnedRequest(item.getRequestId());
        rollbackOutboundQty(item);
        int rows = super.deleteByIdLogic(id);
        if (rows > 0) {
            recalculateRequestFormSummary(item.getRequestId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchLogic(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<RequestItem> items = list(new QueryWrapper<RequestItem>()
                .in("id", ids)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        if (items == null || items.isEmpty()) {
            return 0;
        }
        Set<Long> requestIds = new HashSet<>();
        for (RequestItem item : items) {
            requireOwned(item);
            requireEditableOwnedRequest(item.getRequestId());
            rollbackOutboundQty(item);
            requestIds.add(item.getRequestId());
        }
        int rows = super.deleteBatchLogic(ids);
        if (rows > 0) {
            for (Long requestId : requestIds) {
                recalculateRequestFormSummary(requestId);
            }
        }
        return rows;
    }

    private void rollbackOutboundQty(RequestItem item) {
        if (item == null || item.getStockRecordId() == null) {
            return;
        }
        if (!Integer.valueOf(StockBizConstant.REQUEST_ITEM_STATE_ADDED).equals(item.getState())) {
            return;
        }
        int requestQty = item.getRequestQty() == null ? 0 : Math.abs(item.getRequestQty());
        if (requestQty <= 0) {
            return;
        }
        StockRecord record = stockRecordService.getByIdNotDeleted(item.getStockRecordId());
        if (record == null) {
            throw new RuntimeException("source stock record not found");
        }
        StockOrderItem orderItem = stockOrderItemService.getByIdNotDeleted(record.getOrderItemId());
        if (orderItem == null) {
            throw new RuntimeException("source stock order item not found");
        }
        int originalQty = record.getChangeQty() == null ? 0 : record.getChangeQty();
        int currentQty = orderItem.getChangeQty() == null ? 0 : orderItem.getChangeQty();
        int nextQty = currentQty + requestQty;
        if (nextQty > originalQty) {
            throw new RuntimeException("rollback qty exceeds original outbound qty");
        }
        int affected = stockOrderItemService.getBaseMapper().update(
                null,
                new LambdaUpdateWrapper<StockOrderItem>()
                        .eq(StockOrderItem::getId, orderItem.getId())
                        .eq(StockOrderItem::getDeleted, DeleteEnum.UNDELETED.getCode())
                        .eq(StockOrderItem::getChangeQty, currentQty)
                        .set(StockOrderItem::getChangeQty, nextQty)
        );
        if (affected <= 0) {
            throw new RuntimeException("source stock order item changed concurrently, please retry");
        }

        StockOrder order = stockOrderService.getByIdNotDeleted(record.getOrderId());
        if (order != null) {
            int totalQty = order.getTotalQty() == null ? 0 : order.getTotalQty();
            int orderAffected = stockOrderService.getBaseMapper().update(
                    null,
                    new LambdaUpdateWrapper<StockOrder>()
                            .eq(StockOrder::getId, order.getId())
                            .eq(StockOrder::getDeleted, DeleteEnum.UNDELETED.getCode())
                            .eq(StockOrder::getTotalQty, totalQty)
                            .set(StockOrder::getTotalQty, totalQty + requestQty)
            );
            if (orderAffected <= 0) {
                throw new RuntimeException("source stock order changed concurrently, please retry");
            }
        }
    }

    private void requireOwned(RequestItem item) {
        if (item == null) {
            return;
        }
        requireOwnedRequest(item.getRequestId());
    }

    private RequestForm requireOwnedRequest(Long requestId) {
        if (requestId == null) {
            return null;
        }
        Long userId = UserContext.getUserIdOrDefault();
        RequestForm form = requestFormMapper.selectOne(new QueryWrapper<RequestForm>()
                .eq("id", requestId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (form == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "request form not found");
        }
        if (!permissionQueryService.isSuperAdmin(userId) && !userId.equals(form.getUserId())) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "request item is not owned by current user");
        }
        return form;
    }

    private RequestForm requireEditableOwnedRequest(Long requestId) {
        RequestForm form = requireOwnedRequest(requestId);
        if (form != null && Integer.valueOf(StockBizConstant.REQUEST_STATE_FINISHED).equals(form.getState())) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    "request form is completed and cannot be modified");
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (form != null && !permissionQueryService.isSuperAdmin(userId)
                && !Integer.valueOf(StockBizConstant.REQUEST_STATE_DRAFT).equals(form.getState())
                && !Integer.valueOf(StockBizConstant.REQUEST_STATE_SUBMITTED).equals(form.getState())) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    "request form state is not editable for current user");
        }
        return form;
    }

    private void recalculateRequestFormSummary(Long requestId) {
        if (requestId == null) {
            return;
        }
        RequestForm form = requestFormMapper.selectOne(new QueryWrapper<RequestForm>()
                .eq("id", requestId)
                .eq("deleted", DeleteEnum.UNDELETED.getCode())
                .last("LIMIT 1"));
        if (form == null) {
            return;
        }
        List<RequestItem> items = list(new QueryWrapper<RequestItem>()
                .eq("request_id", requestId)
                .eq("state", StockBizConstant.REQUEST_ITEM_STATE_ADDED)
                .eq("deleted", DeleteEnum.UNDELETED.getCode()));
        int totalQty = 0;
        BigDecimal totalAmt = BigDecimal.ZERO;
        for (RequestItem item : items) {
            totalQty += item.getRequestQty() == null ? 0 : Math.abs(item.getRequestQty());
            if (item.getTotalAmt() != null) {
                totalAmt = totalAmt.add(item.getTotalAmt());
            }
        }
        form.setTotalQty(totalQty);
        form.setRequestQty(totalQty);
        form.setTotalAmt(totalAmt);
        int affected = requestFormMapper.update(
                null,
                new LambdaUpdateWrapper<RequestForm>()
                        .eq(RequestForm::getId, form.getId())
                        .eq(RequestForm::getDeleted, DeleteEnum.UNDELETED.getCode())
                        .set(RequestForm::getTotalQty, totalQty)
                        .set(RequestForm::getRequestQty, totalQty)
                        .set(RequestForm::getTotalAmt, totalAmt)
        );
        if (affected <= 0) {
            throw new RuntimeException("failed to recalculate request form summary");
        }
    }

    private void applyCalculatedTotal(RequestItem item, RequestItem existing) {
        BigDecimal price = firstNonNull(item.getPrice(), existing == null ? null : existing.getPrice(), BigDecimal.ZERO);
        Integer requestQty = firstNonNull(item.getRequestQty(), existing == null ? null : existing.getRequestQty(), 0);
        BigDecimal submittedDiscountPrice = item.getDiscountPrice();
        BigDecimal existingDiscountPrice = existing == null ? null : existing.getDiscountPrice();
        BigDecimal submittedDiscount = item.getDiscount();
        BigDecimal existingDiscount = existing == null ? null : existing.getDiscount();
        boolean discountPriceChanged = submittedDiscountPrice != null
                && (existingDiscountPrice == null || submittedDiscountPrice.compareTo(existingDiscountPrice) != 0);
        boolean discountChanged = submittedDiscount != null
                && (existingDiscount == null || submittedDiscount.compareTo(existingDiscount) != 0);
        BigDecimal discount;
        BigDecimal discountPrice;
        if (discountPriceChanged && !discountChanged) {
            discountPrice = submittedDiscountPrice.setScale(2, RoundingMode.HALF_UP);
            discount = price.signum() == 0
                    ? BigDecimal.ZERO
                    : discountPrice.divide(price, 4, RoundingMode.HALF_UP);
        } else {
            discount = firstNonNull(submittedDiscount, existingDiscount, BigDecimal.ONE);
            discountPrice = price.multiply(discount).setScale(2, RoundingMode.HALF_UP);
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.ONE) > 0) {
            throw new co.handk.backend.exception.BusinessException(
                    co.handk.backend.constant.MessageKeyConstant.ERROR_RUNTIME,
                    "discount must be between 0 and 1");
        }
        item.setDiscount(discount);
        item.setDiscountPrice(discountPrice);
        item.setTotalAmt(discountPrice
                .multiply(BigDecimal.valueOf(Math.abs(requestQty)))
                .setScale(2, RoundingMode.HALF_UP));
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
