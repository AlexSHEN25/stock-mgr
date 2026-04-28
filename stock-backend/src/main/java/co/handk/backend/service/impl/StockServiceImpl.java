package co.handk.backend.service.impl;

import co.handk.backend.entity.Stock;
import co.handk.backend.mapper.StockMapper;
import co.handk.backend.service.StockService;
import co.handk.common.model.vo.StockVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl extends BaseServiceImpl<StockMapper, Stock, StockVO> implements StockService {

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
