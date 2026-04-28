package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsSku;
import co.handk.backend.mapper.GoodsSkuMapper;
import co.handk.backend.service.GoodsSkuService;
import co.handk.common.model.vo.GoodsSkuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class GoodsSkuServiceImpl extends BaseServiceImpl<GoodsSkuMapper, GoodsSku, GoodsSkuVO>
        implements GoodsSkuService {

    @Override
    protected GoodsSkuVO toVO(GoodsSku entity) {
        if (entity == null) {
            return null;
        }
        GoodsSkuVO vo = new GoodsSkuVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> GoodsSku toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        GoodsSku entity = new GoodsSku();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}