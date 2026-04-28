package co.handk.backend.service.impl;

import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.mapper.StockOrderItemMapper;
import co.handk.backend.service.StockOrderItemService;
import co.handk.common.model.vo.StockOrderItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class StockOrderItemServiceImpl extends BaseServiceImpl<StockOrderItemMapper, StockOrderItem, StockOrderItemVO>
        implements StockOrderItemService {

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
}