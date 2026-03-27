package co.handk.backend.controller;

import co.handk.backend.service.StockService;
import co.handk.backend.entity.Stock;
import co.handk.common.model.dto.StockDTO;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.StockPageQueryDTO;
import co.handk.common.model.vo.StockPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 库存接口
 */
@RestController
@Validated
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * 新增库存
     */
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid StockDTO dto) {
        return stockService.create(dto);
    }

    /**
     * 根据ID查询库存
     */
    @GetMapping("/{id}")
    public Stock get(@PathVariable @NotNull Long id) {
        return stockService.get(id);
    }

    /**
     * 修改库存
     */
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid StockDTO dto) {
        return stockService.update(dto);
    }

    /**
     * 删除库存
     */
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return stockService.delete(id);
    }

    /**
     * 查询全部库存
     */
    @GetMapping("/list")
    public List<Stock> list() {
        return stockService.listAll();
    }

    /**
     * 分页查询库存
     * 示例：
     * GET /stock/page?pageNum=1&pageSize=10&goodsName=苹果&sku=A001&warehouseId=1&status=1
     */
    @GetMapping("/page")
    public PageResult<StockPageVO> page(@Valid StockPageQueryDTO dto) {
        return stockService.pageQuery(dto);
    }
}
