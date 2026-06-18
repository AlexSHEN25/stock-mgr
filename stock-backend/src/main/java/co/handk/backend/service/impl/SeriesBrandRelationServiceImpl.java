package co.handk.backend.service.impl;

import co.handk.backend.annotation.context.UserContext;
import co.handk.backend.entity.SeriesBrandRelation;
import co.handk.backend.mapper.SeriesBrandRelationMapper;
import co.handk.backend.service.SeriesBrandRelationService;
import co.handk.common.model.vo.SeriesBrandRelationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeriesBrandRelationServiceImpl extends BaseServiceImpl<SeriesBrandRelationMapper, SeriesBrandRelation, SeriesBrandRelationVO>
        implements SeriesBrandRelationService {
    @Override
    protected SeriesBrandRelationVO toVO(SeriesBrandRelation entity) {
        if (entity == null) {
            return null;
        }
        SeriesBrandRelationVO vo = new SeriesBrandRelationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected SeriesBrandRelation toEntity(Object dto) {
        if (dto == null) {
            return null;
        }
        SeriesBrandRelation entity = new SeriesBrandRelation();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <C> boolean saveByDto(C dto) {
        SeriesBrandRelation entity = toEntity(dto);
        if (entity == null || entity.getSeriesId() == null || entity.getBrandId() == null) {
            return super.saveByDto(dto);
        }
        return baseMapper.upsertRelation(entity.getSeriesId(), entity.getBrandId(), UserContext.getUserIdOrDefault()) > 0;
    }
}
