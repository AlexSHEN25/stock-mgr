package co.handk.backend.service.impl;

import co.handk.backend.entity.BrandMakerRelation;
import co.handk.backend.mapper.BrandMakerRelationMapper;
import co.handk.backend.service.BrandMakerRelationService;
import co.handk.common.model.vo.BrandMakerRelationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class BrandMakerRelationServiceImpl extends BaseServiceImpl<BrandMakerRelationMapper, BrandMakerRelation, BrandMakerRelationVO>
        implements BrandMakerRelationService {

    @Override
    protected BrandMakerRelationVO toVO(BrandMakerRelation entity) {
        if (entity == null) {
            return null;
        }
        BrandMakerRelationVO vo = new BrandMakerRelationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> BrandMakerRelation toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        BrandMakerRelation entity = new BrandMakerRelation();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}