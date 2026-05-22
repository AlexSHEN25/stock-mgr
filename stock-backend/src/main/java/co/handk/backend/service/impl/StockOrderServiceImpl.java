package co.handk.backend.service.impl;

import co.handk.backend.context.UserContext;
import co.handk.backend.entity.StockOrder;
import co.handk.backend.mapper.StockOrderMapper;
import co.handk.backend.service.PermissionQueryService;
import co.handk.backend.service.StockOrderService;
import co.handk.common.model.vo.StockOrderVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
}
