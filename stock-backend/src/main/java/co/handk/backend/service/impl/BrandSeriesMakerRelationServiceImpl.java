package co.handk.backend.service.impl;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.BrandSeriesMakerRelation;
import co.handk.backend.mapper.BrandSeriesMakerRelationMapper;
import co.handk.backend.service.BrandSeriesMakerRelationService;
import co.handk.common.model.vo.BrandSeriesMakerRelationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandSeriesMakerRelationServiceImpl extends BaseServiceImpl<BrandSeriesMakerRelationMapper, BrandSeriesMakerRelation, BrandSeriesMakerRelationVO>
        implements BrandSeriesMakerRelationService {

    @Override
    protected BrandSeriesMakerRelationVO toVO(BrandSeriesMakerRelation entity) {
        if (entity == null) {
            return null;
        }
        BrandSeriesMakerRelationVO vo = new BrandSeriesMakerRelationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> BrandSeriesMakerRelation toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        BrandSeriesMakerRelation entity = new BrandSeriesMakerRelation();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        BrandSeriesMakerRelation entity = toEntity(dto);
        if (entity == null || entity.getBrandId() == null || entity.getSeriesId() == null || entity.getMakerId() == null) {
            return super.saveByDto(dto);
        }
        return baseMapper.upsertRelation(
                entity.getBrandId(),
                entity.getSeriesId(),
                entity.getMakerId(),
                UserContext.getUserIdOrDefault()
        ) > 0;
    }
}
