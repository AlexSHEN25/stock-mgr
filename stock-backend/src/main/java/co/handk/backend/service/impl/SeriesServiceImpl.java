package co.handk.backend.service.impl;

import co.handk.backend.entity.Series;
import co.handk.backend.mapper.SeriesMapper;
import co.handk.backend.service.SeriesService;
import co.handk.common.model.vo.SeriesVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class SeriesServiceImpl extends BaseServiceImpl<SeriesMapper, Series, SeriesVO>
        implements SeriesService {

    @Override
    protected SeriesVO toVO(Series entity) {
        if (entity == null) {
            return null;
        }
        SeriesVO vo = new SeriesVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    protected <D> Series toEntity(D dto) {
        if (dto == null) {
            return null;
        }
        Series entity = new Series();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}