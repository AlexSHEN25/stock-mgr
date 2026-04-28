package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsSkuSpec;
import co.handk.backend.mapper.GoodsSkuSpecMapper;
import co.handk.backend.service.GoodsSkuSpecService;
import co.handk.common.model.vo.GoodsSkuSpecVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class GoodsSkuSpecServiceImpl extends BaseServiceImpl<GoodsSkuSpecMapper, GoodsSkuSpec, GoodsSkuSpecVO>
        implements GoodsSkuSpecService {

    @Override
    protected GoodsSkuSpecVO toVO(GoodsSkuSpec entity) {
        if (entity == null) {
            return null;
        }
        GoodsSkuSpecVO vo = new GoodsSkuSpecVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> GoodsSkuSpec toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        GoodsSkuSpec entity = new GoodsSkuSpec();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}