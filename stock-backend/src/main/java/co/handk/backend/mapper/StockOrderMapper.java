package co.handk.backend.mapper;

import co.handk.backend.entity.StockOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockOrderMapper extends BaseMapper<StockOrder> {
}
