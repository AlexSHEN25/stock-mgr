package co.handk.backend.service.impl;

import co.handk.backend.entity.StockOrder;
import co.handk.backend.mapper.StockOrderMapper;
import co.handk.backend.service.StockOrderService;
import co.handk.common.model.vo.StockOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class StockOrderServiceImpl extends BaseServiceImpl<StockOrderMapper, StockOrder, StockOrderVO>
        implements StockOrderService {

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
}