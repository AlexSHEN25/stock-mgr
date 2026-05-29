package co.handk.backend.service.impl;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.context.UserContext;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.mapper.StockOrderMapper;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.StockOrderService;
import co.handk.common.constant.StockBizConstant;
import co.handk.common.model.dto.create.CreateStockOrderDTO;
import co.handk.common.model.dto.update.UpdateStockOrderDTO;
import co.handk.common.model.vo.StockOrderVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StockOrderServiceImpl extends BaseServiceImpl<StockOrderMapper, StockOrder, StockOrderVO>
        implements StockOrderService {

    private final PermissionQueryService permissionQueryService;

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
        return super.updateByDto(dto);
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

    private void validateRequiredDateByOrderType(Integer orderType, LocalDateTime bizDate) {
        if (orderType == null) {
            return;
        }
        if ((orderType.equals(StockBizConstant.ORDER_TYPE_INBOUND)
                || orderType.equals(StockBizConstant.ORDER_TYPE_OUTBOUND)) && bizDate == null) {
            throw new BusinessException(MessageKeyConstant.ERROR_RUNTIME, "入出庫伝票では業務日付が必須です");
        }
    }
}
