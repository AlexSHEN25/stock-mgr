package co.handk.backend.service.impl;

import co.handk.backend.entity.StockType;
import co.handk.backend.mapper.StockTypeMapper;
import co.handk.backend.service.StockTypeService;
import co.handk.common.model.vo.StockTypeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class GoodsTypeServiceImpl extends BaseServiceImpl<StockTypeMapper, StockType, StockTypeVO>
        implements StockTypeService {

    @Override
    protected StockTypeVO toVO(StockType entity) {
        if (entity == null) {
            return null;
        }
        StockTypeVO vo = new StockTypeVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> StockType toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        StockType entity = new StockType();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}