package co.handk.backend.service.impl;

import co.handk.backend.annotation.JoinQueryConfig;
import co.handk.backend.annotation.JoinTable;
import co.handk.backend.annotation.JoinType;
import co.handk.backend.entity.Series;
import co.handk.backend.mapper.SeriesMapper;
import co.handk.backend.service.SeriesService;
import co.handk.common.model.vo.SeriesVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@JoinQueryConfig(
        baseTable = "t_series",
        baseAlias = "t",
        joins = {
                @JoinTable(type = JoinType.LEFT, table = "t_brand", alias = "b", on = "b.id = t.brand_id AND b.deleted = 0")
        }
)
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
