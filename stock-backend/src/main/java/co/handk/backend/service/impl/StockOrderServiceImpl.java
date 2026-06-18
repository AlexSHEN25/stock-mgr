package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.Message;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.entity.StockType;
import co.handk.backend.entity.User;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.StockOrderMapper;
import co.handk.backend.service.MessageService;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.StockOrderService;
import co.handk.backend.service.StockTypeService;
import co.handk.backend.service.UserService;
import co.handk.common.constant.MessageBizConstant;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.model.dto.create.CreateStockOrderDTO;
import co.handk.common.model.dto.update.UpdateStockOrderDTO;
import co.handk.common.model.vo.StockOrderVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class StockOrderServiceImpl extends BaseServiceImpl<StockOrderMapper, StockOrder, StockOrderVO>
        implements StockOrderService {

    private final PermissionQueryService permissionQueryService;
    private final MessageService messageService;
    private final UserService userService;
    private final StockTypeService stockTypeService;
    private static final DateTimeFormatter ORDER_NO_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_STOCK_TYPE_NAME = "通常品";

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
        Long userId = UserContext.getUserIdOrDefault();
        if (entity.getId() == null && !permissionQueryService.isSuperAdmin(userId)) {
            entity.setRequesterId(userId);
            entity.setOperatorId(userId);
        }
        return entity;
    }

    @Override
    protected <Q> QueryWrapper<StockOrder> buildWrapper(Q dto) {
        QueryWrapper<StockOrder> wrapper = super.buildWrapper(dto);
        if (dto instanceof co.handk.common.model.dto.query.StockOrderQueryDTO query) {
            if (query.getOutboundMode() != null && !query.getOutboundMode().isBlank()) {
                wrapper.eq("outbound_mode", query.getOutboundMode().trim());
            }
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (!permissionQueryService.isSuperAdmin(userId)) {
            wrapper.and(w -> w.eq("requester_id", userId).or().eq("operator_id", userId));
        }
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        if (dto instanceof CreateStockOrderDTO createDto) {
            prepareCreate(createDto);
        }
        validateRequiredDateByOrderType(dto);
        return super.saveByDto(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <U> boolean updateByDto(U dto) {
        if (!(dto instanceof UpdateStockOrderDTO updateDto)) {
            return super.updateByDto(dto);
        }
        StockOrder before = getByIdNotDeleted(updateDto.getId());
        prepareUpdate(updateDto, before);
        validateRequiredDateByOrderType(dto);
        if (isInboundOrOutbound(updateDto.getOrderType())
                && Integer.valueOf(StockBizConstant.ORDER_STATE_FINISHED).equals(updateDto.getState())
                && (before == null || !Integer.valueOf(StockBizConstant.ORDER_STATE_FINISHED).equals(before.getState()))) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME,
                    "stock orders can only be completed through administrator approval");
        }
        Integer beforeState = before == null ? null : before.getState();
        boolean updated = super.updateByDto(updateDto);
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

    @Override
    public StockOrder getByIdNotDeleted(Serializable id) {
        StockOrder order = super.getByIdNotDeleted(id);
        requireOwned(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIdLogic(Long id) {
        StockOrder order = super.getByIdNotDeleted(id);
        requireOwned(order);
        return super.deleteByIdLogic(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchLogic(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return super.deleteBatchLogic(ids);
        }
        int rows = 0;
        for (Long id : ids) {
            StockOrder order = super.getByIdNotDeleted(id);
            requireOwned(order);
            rows += super.deleteByIdLogic(id);
        }
        return rows;
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

    private boolean isInboundOrOutbound(Integer orderType) {
        return Integer.valueOf(StockBizConstant.ORDER_TYPE_INBOUND).equals(orderType)
                || Integer.valueOf(StockBizConstant.ORDER_TYPE_OUTBOUND).equals(orderType);
    }

    private void prepareCreate(CreateStockOrderDTO dto) {
        Long userId = UserContext.getUserIdOrDefault();
        boolean admin = permissionQueryService.isSuperAdmin(userId);
        dto.setOrderNo(generateOrderNo());
        if (dto.getBizDate() == null) {
            dto.setBizDate(LocalDate.now());
        }
        if (dto.getSourceType() == null) {
            dto.setSourceType(StockBizConstant.SOURCE_TYPE_MANUAL);
        }
        validateSourceType(dto.getSourceType(), admin);
        if (dto.getState() == null) {
            dto.setState(StockBizConstant.ORDER_STATE_DRAFT);
        }
        validateState(dto.getState(), admin);
        applyCurrentUser(dto, userId);
        dto.setTotalQty(0);
        dto.setStockTypeId(resolveStockTypeId(dto.getStockTypeId()));
        dto.setApproverId(null);
        dto.setApproverName(null);
        dto.setApproveTime(null);
        dto.setFinishTime(null);
        if (Integer.valueOf(StockBizConstant.SOURCE_TYPE_MANUAL).equals(dto.getSourceType())) {
            dto.setSourceId(null);
        }
    }

    private void prepareUpdate(UpdateStockOrderDTO dto, StockOrder before) {
        Long userId = UserContext.getUserIdOrDefault();
        boolean admin = permissionQueryService.isSuperAdmin(userId);
        Integer requestedState = dto.getState();
        if (before != null) {
            dto.setOrderNo(before.getOrderNo());
            if (dto.getBizDate() == null) {
                dto.setBizDate(before.getBizDate());
            }
            dto.setRequesterId(before.getRequesterId());
            dto.setRequesterName(before.getRequesterName());
            dto.setOperatorId(before.getOperatorId());
            dto.setOperatorName(before.getOperatorName());
            dto.setApproverId(before.getApproverId());
            dto.setApproverName(before.getApproverName());
            dto.setApproveTime(before.getApproveTime());
            dto.setFinishTime(before.getFinishTime());
            dto.setTotalQty(calculateOrderTotalQty(before.getId()));
            if (dto.getStockTypeId() == null) {
                dto.setStockTypeId(before.getStockTypeId());
            }
            if (!admin) {
                dto.setState(before.getState());
            }
        } else {
            dto.setTotalQty(0);
        }
        if (dto.getBizDate() == null) {
            dto.setBizDate(LocalDate.now());
        }
        if (dto.getSourceType() == null) {
            dto.setSourceType(before == null || before.getSourceType() == null
                    ? StockBizConstant.SOURCE_TYPE_MANUAL : before.getSourceType());
        }
        validateSourceType(dto.getSourceType(), admin);
        if (dto.getState() == null) {
            dto.setState(before == null || before.getState() == null
                    ? StockBizConstant.ORDER_STATE_DRAFT : before.getState());
        }
        validateState(dto.getState(), admin);
        if (admin && before != null && hasStateChanged(before.getState(), dto.getState())) {
            applyApprovalAuditFields(dto, userId);
            if (Integer.valueOf(StockBizConstant.ORDER_STATE_FINISHED).equals(dto.getState())) {
                dto.setFinishTime(dto.getApproveTime());
            }
        } else if (admin && before != null && requestedState == null) {
            dto.setState(before.getState());
        }
        dto.setStockTypeId(resolveStockTypeId(dto.getStockTypeId()));
        if (Integer.valueOf(StockBizConstant.SOURCE_TYPE_MANUAL).equals(dto.getSourceType())) {
            dto.setSourceId(null);
        }
    }

    private void applyCurrentUser(CreateStockOrderDTO dto, Long userId) {
        dto.setRequesterId(userId);
        dto.setOperatorId(userId);
        User user = userService.getByIdNotDeleted(userId);
        String username = user == null ? null : user.getUsername();
        dto.setRequesterName(username);
        dto.setOperatorName(username);
    }

    private void applyApprovalAuditFields(UpdateStockOrderDTO dto, Long userId) {
        dto.setApproverId(userId);
        User user = userService.getByIdNotDeleted(userId);
        dto.setApproverName(user == null ? null : user.getUsername());
        dto.setApproveTime(LocalDateTime.now());
    }

    private Long resolveStockTypeId(Long stockTypeId) {
        if (stockTypeId != null) {
            return stockTypeId;
        }
        StockType stockType = stockTypeService.getOne(new QueryWrapper<StockType>()
                .eq("name", DEFAULT_STOCK_TYPE_NAME)
                .last("LIMIT 1"));
        return stockType == null ? null : stockType.getId();
    }

    private void validateSourceType(Integer sourceType, boolean admin) {
        if (sourceType == null) {
            return;
        }
        boolean allowed = admin
                ? sourceType >= StockBizConstant.SOURCE_TYPE_ORDER && sourceType <= StockBizConstant.SOURCE_TYPE_MANUAL
                : sourceType.equals(StockBizConstant.SOURCE_TYPE_REQUEST)
                || sourceType.equals(StockBizConstant.SOURCE_TYPE_MANUAL);
        if (!allowed) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "現在のユーザーはこの元種別を設定できません");
        }
    }

    private void validateState(Integer state, boolean admin) {
        if (state == null) {
            return;
        }
        boolean allowed = admin
                ? state >= StockBizConstant.ORDER_STATE_DRAFT && state <= StockBizConstant.ORDER_STATE_CANCELED
                : state.equals(StockBizConstant.ORDER_STATE_DRAFT)
                || state.equals(StockBizConstant.ORDER_STATE_APPROVING);
        if (!allowed) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "現在のユーザーはこの在庫伝票状態を設定できません");
        }
    }

    private int calculateOrderTotalQty(Long orderId) {
        if (orderId == null) {
            return 0;
        }
        List<Object> qtyList = getBaseMapper().selectObjs(new QueryWrapper<StockOrder>()
                .select("(SELECT COALESCE(SUM(change_qty), 0) FROM t_stock_order_item soi "
                        + "WHERE soi.order_id = t_stock_order.id AND soi.deleted = "
                        + DeleteEnum.UNDELETED.getCode() + ") AS total_qty")
                .eq("id", orderId));
        if (qtyList == null || qtyList.isEmpty() || qtyList.get(0) == null) {
            return 0;
        }
        return ((Number) qtyList.get(0)).intValue();
    }

    private String generateOrderNo() {
        for (int i = 0; i < 5; i++) {
            String candidate = "SO" + LocalDateTime.now().format(ORDER_NO_TIME_FORMAT)
                    + ThreadLocalRandom.current().nextInt(1000, 10000);
            long count = count(new QueryWrapper<StockOrder>()
                    .eq("order_no", candidate));
            if (count == 0) {
                return candidate;
            }
        }
        throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "在庫注文番号の生成に失敗しました");
    }

    private void requireOwned(StockOrder order) {
        if (order == null) {
            return;
        }
        Long userId = UserContext.getUserIdOrDefault();
        if (permissionQueryService.isSuperAdmin(userId)) {
            return;
        }
        if (!userId.equals(order.getRequesterId()) && !userId.equals(order.getOperatorId())) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "現在のユーザーはこの在庫注文を操作できません");
        }
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
