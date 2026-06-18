package co.handk.backend.mapper;

import co.handk.backend.entity.BrandMakerRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BrandMakerRelationMapper extends BaseMapper<BrandMakerRelation> {
    @Insert("""
            INSERT INTO t_brand_maker_relation
                (brand_id, maker_id, deleted, created_by, updated_by, create_time, update_time)
            VALUES
                (#{brandId}, #{makerId}, 0, #{userId}, #{userId}, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                deleted = 0,
                updated_by = VALUES(updated_by),
                update_time = NOW()
            """)
    int upsertRelation(@Param("brandId") Long brandId,
                       @Param("makerId") Long makerId,
                       @Param("userId") Long userId);
}
