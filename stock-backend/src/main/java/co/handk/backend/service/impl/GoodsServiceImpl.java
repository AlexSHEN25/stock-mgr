package co.handk.backend.service.impl;

import co.handk.backend.entity.Goods;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.service.GoodsService;
import co.handk.common.model.vo.GoodsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl extends BaseServiceImpl<GoodsMapper, Goods, GoodsVO>
        implements GoodsService {

    @Override
    protected GoodsVO toVO(Goods entity) {
        if (entity == null) {
            return null;
        }
        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Goods toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Goods entity = new Goods();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}