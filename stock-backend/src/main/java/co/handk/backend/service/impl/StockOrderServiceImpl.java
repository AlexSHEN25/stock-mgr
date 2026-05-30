package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.Message;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.StockOrderMapper;
import co.handk.backend.service.MessageService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.StockOrderService;
import co.handk.common.constant.MessageBizConstant;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.model.dto.create.CreateStockOrderDTO;
import co.handk.common.model.dto.update.UpdateStockOrderDTO;
import co.handk.common.model.vo.StockOrderVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockOrderServiceImpl extends BaseServiceImpl<StockOrderMapper, StockOrder, StockOrderVO>
        implements StockOrderService {

    private final PermissionQueryService permissionQueryService;
    private final MessageService messageService;

    @Override
    protected StockOrderVO toVO(StockOrder entity) {
        if (entity == null) {
            return null;
        }
        StockOrderVO vo = new StockOrderVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> StockOrder toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        StockOrder entity = new StockOrder();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    protected <Q> QueryWrapper<StockOrder> buildWrapper(Q dto) {
        QueryWrapper<StockOrder> wrapper = super.buildWrapper(dto);
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.and(w -> w.eq("requester_id", userId).or().eq("operator_id", userId));
        }
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        validateRequiredDateByOrderType(dto);
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        validateRequiredDateByOrderType(dto);
        if (!(dto instanceof UpdateStockOrderDTO updateDto)) {
            return super.updateByDto(dto);
        }
        StockOrder before = getByIdNotDeleted(updateDto.getId());
        Integer beforeState = before == null ? null : before.getState();
        boolean updated = super.updateByDto(dto);
        if (!updated) {
            return false;
        }
        StockOrder after = getByIdNotDeleted(updateDto.getId());
        Integer afterState = after == null ? null : after.getState();
        if (after != null && hasStateChanged(beforeState, afterState)) {
            notifyStockOrderStateChanged(after, beforeState, afterState);
        }
        return true;
    }

    private void validateRequiredDateByOrderType(Object dto) {
        if (dto instanceof CreateStockOrderDTO createDto) {
            validateRequiredDateByOrderType(createDto.getOrderType(), createDto.getBizDate());
            return;
        }
        if (dto instanceof UpdateStockOrderDTO updateDto) {
            validateRequiredDateByOrderType(updateDto.getOrderType(), updateDto.getBizDate());
        }
    }

    private void validateRequiredDateByOrderType(Integer orderType, LocalDate bizDate) {
        if (orderType == null) {
            return;
        }
        if ((orderType.equals(StockBizConstant.ORDER_TYPE_INBOUND)
                || orderType.equals(StockBizConstant.ORDER_TYPE_OUTBOUND)) && bizDate == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "入出庫伝票では業務日付が必須です");
        }
    }

    private boolean hasStateChanged(Integer beforeState, Integer afterState) {
        if (beforeState == null && afterState == null) {
            return false;
        }
        if (beforeState == null || afterState == null) {
            return true;
        }
        return !beforeState.equals(afterState);
    }

    private void notifyStockOrderStateChanged(StockOrder order, Integer beforeState, Integer afterState) {
        Set<Long> targetUserIds = new HashSet<>();
        if (order.getRequesterId() != null) {
            targetUserIds.add(order.getRequesterId());
        }
        if (order.getOperatorId() != null) {
            targetUserIds.add(order.getOperatorId());
        }
        if (targetUserIds.isEmpty()) {
            return;
        }
        String text = String.format(
                "入出庫伝票[%s]の状態が%sから%sに変更されました",
                order.getOrderNo() == null ? "-" : order.getOrderNo(),
                toOrderStateName(beforeState),
                toOrderStateName(afterState)
        );
        for (Long userId : targetUserIds) {
            Message message = new Message();
            message.setType(MessageBizConstant.TYPE_STOCK_ORDER);
            message.setUserId(userId);
            message.setMessage(text);
            message.setSourceId(order.getId() == null ? 0 : order.getId().intValue());
            message.setIsRead(MessageBizConstant.IS_UNREAD);
            message.setState(MessageBizConstant.STATE_SENT);
            messageService.save(message);
        }
    }

    private String toOrderStateName(Integer state) {
        if (state == null) {
            return "不明";
        }
        if (state.equals(StockBizConstant.ORDER_STATE_DRAFT)) {
            return "下書き";
        }
        if (state.equals(StockBizConstant.ORDER_STATE_APPROVING)) {
            return "承認中";
        }
        if (state.equals(StockBizConstant.ORDER_STATE_FINISHED)) {
            return "完了";
        }
        if (state.equals(StockBizConstant.ORDER_STATE_CANCELED)) {
            return "取消";
        }
        return String.valueOf(state);
    }
}
