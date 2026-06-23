package co.handk.backend.mapper;

import co.handk.backend.entity.Brand;
import co.handk.common.model.dto.query.BrandHierarchyQueryDTO;
import co.handk.common.model.vo.BrandHierarchyVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BrandMapper extends BaseMapper<Brand> {
    long countHierarchy(@Param("query") BrandHierarchyQueryDTO query);

    List<BrandHierarchyVO> selectHierarchy(@Param("query") BrandHierarchyQueryDTO query,
                                           @Param("offset") long offset,
                                           @Param("limit") long limit);
}
