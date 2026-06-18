package co.handk.backend.mapper;

import co.handk.backend.entity.SeriesBrandRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeriesBrandRelationMapper extends BaseMapper<SeriesBrandRelation> {
    @Insert("""
            INSERT INTO t_series_brand_relation
                (series_id, brand_id, deleted, created_by, updated_by, create_time, update_time)
            VALUES
                (#{seriesId}, #{brandId}, 0, #{userId}, #{userId}, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                deleted = 0,
                updated_by = VALUES(updated_by),
                update_time = NOW()
            """)
    int upsertRelation(@Param("seriesId") Long seriesId,
                       @Param("brandId") Long brandId,
                       @Param("userId") Long userId);
}
