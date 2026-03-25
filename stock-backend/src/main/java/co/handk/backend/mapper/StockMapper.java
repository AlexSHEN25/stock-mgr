package co.handk.backend.mapper;

import co.handk.backend.entity.Stock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存 Mapper
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}