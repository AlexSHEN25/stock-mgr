package co.handk.backend.mapper;

import co.handk.backend.entity.Goods;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.vo.GoodsVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {


    List<GoodsVO> selectGoodsPage(@Param("q") GoodsQueryDTO query,
                                  @Param("offset") long offset,
                                  @Param("size") long size);

    Long countGoodsPage(@Param("q") GoodsQueryDTO queryDTO);
}
