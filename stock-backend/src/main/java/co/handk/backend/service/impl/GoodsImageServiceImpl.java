package co.handk.backend.service.impl;

import co.handk.backend.entity.GoodsImage;
import co.handk.backend.mapper.GoodsImageMapper;
import co.handk.backend.service.GoodsImageService;
import co.handk.common.model.vo.GoodsImageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class GoodsImageServiceImpl extends BaseServiceImpl<GoodsImageMapper, GoodsImage, GoodsImageVO>
        implements GoodsImageService {

    @Override
    protected GoodsImageVO toVO(GoodsImage entity) {
        if (entity == null) {
            return null;
        }
        GoodsImageVO vo = new GoodsImageVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> GoodsImage toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        GoodsImage entity = new GoodsImage();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}