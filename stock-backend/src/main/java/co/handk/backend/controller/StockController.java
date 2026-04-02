package co.handk.backend.controller;

import co.handk.backend.service.StockService;
import co.handk.common.model.dto.create.CreateStockDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.StockPageQueryDTO;
import co.handk.common.model.vo.StockPageVO;
import co.handk.common.model.vo.StockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

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
    public Boolean create(@RequestBody @NotNull @Valid CreateStockDTO dto) {
        return stockService.create(dto);
    }

    /**
     * 根据ID查询库存
     */
    @GetMapping("/{id}")
    public StockVO get(@PathVariable @NotNull Long id) {
        return stockService.get(id);
    }

    /**
     * 修改库存
     */
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockDTO dto) {
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
     * 条件分页查询库存
     */

    /**
     * 分页查询库存
     * 示例：
     * GET /stock/page?pageNum=1&pageSize=10&goodsName=苹果&sku=A001&warehouseId=1&status=1
     */
    @GetMapping("/page")
    public PageResult<StockPageVO> page(@Valid StockPageQueryDTO dto) {
        return stockService.pageQuery(dto);
    }

    /**
     * 撤销操作：根据库存流水ID回滚库存
     */
    @PostMapping("/undo/{stockRecordId}")
    public Boolean undo(@PathVariable @NotNull Long stockRecordId) {
        return stockService.undo(stockRecordId);
    }

    /**
     * 重做操作：根据库存流水ID恢复库存
     */
    @PostMapping("/redo/{stockRecordId}")
    public Boolean redo(@PathVariable @NotNull Long stockRecordId) {
        return stockService.redo(stockRecordId);
    }
}
