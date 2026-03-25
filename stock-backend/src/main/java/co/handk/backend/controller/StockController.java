package co.handk.backend.controller;

import co.handk.backend.service.StockService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.StockPageQueryDTO;
import co.handk.common.model.vo.StockPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存接口
 */
@RestController
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * 分页查询库存
     * 示例：
     * GET /stock/page?pageNum=1&pageSize=10&goodsName=苹果&sku=A001&warehouseId=1&status=1
     */
    @GetMapping("/page")
    public PageResult<StockPageVO> page(StockPageQueryDTO dto) {
        return stockService.pageQuery(dto);
    }
}