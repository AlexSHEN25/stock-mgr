package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsLevelPrice;
import co.handk.backend.mapper.GoodsLevelPriceMapper;
import co.handk.backend.service.GoodsLevelPriceService;
import co.handk.common.model.vo.GoodsLevelPriceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class GoodsLevelPriceServiceImpl extends BaseServiceImpl<GoodsLevelPriceMapper, GoodsLevelPrice, GoodsLevelPriceVO>
        implements GoodsLevelPriceService {

    @Override
    protected GoodsLevelPriceVO toVO(GoodsLevelPrice entity) {
        if (entity == null) {
            return null;
        }
        GoodsLevelPriceVO vo = new GoodsLevelPriceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> GoodsLevelPrice toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        GoodsLevelPrice entity = new GoodsLevelPrice();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}