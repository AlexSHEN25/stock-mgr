package co.handk.backend.mapper;

import co.handk.backend.entity.Goods;
import co.handk.common.model.dto.query.GoodsBundleQueryDTO;
import co.handk.common.model.vo.GoodsBundleVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    Long countBundlePage(@Param("q") GoodsBundleQueryDTO query);

    List<GoodsBundleVO> selectBundlePage(@Param("q") GoodsBundleQueryDTO query,
                                         @Param("offset") long offset,
                                         @Param("size") long size);
}
