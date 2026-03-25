package co.handk.backend.service;

import co.handk.backend.entity.Stock;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.StockPageQueryDTO;
import co.handk.common.model.vo.StockPageVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

/**
 * 库存 Service
 */
@Service
public interface StockService  extends IService<Stock> {

    /**
     * 分页查询库存
     */
    PageResult<StockPageVO> pageQuery(StockPageQueryDTO dto);
}