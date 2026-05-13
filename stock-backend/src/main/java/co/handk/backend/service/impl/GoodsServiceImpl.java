package co.handk.backend.service.impl;

import co.handk.backend.annotation.JoinQueryConfig;
import co.handk.backend.annotation.JoinTable;
import co.handk.backend.entity.Goods;
import co.handk.backend.enums.JoinType;
import co.handk.backend.mapper.GoodsMapper;
import co.handk.backend.service.GoodsService;
import co.handk.common.model.vo.GoodsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@JoinQueryConfig(
        baseTable = "t_goods",
        baseAlias = "t",
        joins = {
                @JoinTable(type = JoinType.LEFT, table = "t_brand", alias = "b", on = "b.id = t.brand_id"),
                @JoinTable(type = JoinType.LEFT, table = "t_series", alias = "s", on = "s.id = t.series_id"),
                @JoinTable(type = JoinType.LEFT, table = "t_category", alias = "c", on = "c.id = t.category_id"),
                @JoinTable(type = JoinType.LEFT, table = "t_maker", alias = "m", on = "m.id = t.maker_id")
        }
)
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
