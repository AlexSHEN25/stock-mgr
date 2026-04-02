package co.handk.api;

import co.handk.common.model.dto.create.CreateStockDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.StockPageQueryDTO;
import co.handk.common.model.vo.StockPageVO;
import co.handk.common.model.vo.StockVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 库存接口
 */
@Validated
@RequestMapping("/stock")
public interface StockApi {


    /**
     * 新增库存
     */
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateStockDTO dto);

    /**
     * 根据ID查询库存
     */
    @GetMapping("/{id}")
    StockVO get(@PathVariable @NotNull Long id);

    /**
     * 修改库存
     */
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateStockDTO dto);

    /**
     * 删除库存
     */
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);

    /**
     * 条件分页查询库存
     */

    /**
     * 分页查询库存
     * 示例：
     * GET /stock/page?pageNum=1&pageSize=10&goodsName=苹果&sku=A001&warehouseId=1&status=1
     */
    @GetMapping("/page")
    PageResult<StockPageVO> page(@Valid StockPageQueryDTO dto);

    /**
     * 撤销操作：根据库存流水ID回滚库存
     */
    @PostMapping("/undo/{stockRecordId}")
    Boolean undo(@PathVariable @NotNull Long stockRecordId);

    /**
     * 重做操作：根据库存流水ID恢复库存
     */
    @PostMapping("/redo/{stockRecordId}")
    Boolean redo(@PathVariable @NotNull Long stockRecordId);
}
