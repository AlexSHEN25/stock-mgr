package co.handk.backend.controller;

import co.handk.common.model.vo.StockOrderItemVO;
import co.handk.common.model.dto.create.CreateStockOrderItemDTO;
import co.handk.common.model.dto.update.UpdateStockOrderItemDTO;
import co.handk.backend.service.StockOrderItemService;
import co.handk.common.model.dto.query.StockOrderItemQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/stockOrderItem")
public class StockOrderItemController {
    @Autowired
    private StockOrderItemService stockOrderItemService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockOrderItemDTO dto) {
        return stockOrderItemService.create(dto);
    }
    @GetMapping("/{id}")
    public StockOrderItemVO get(@PathVariable @NotNull Long id) {
        return stockOrderItemService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockOrderItemDTO dto) {
        return stockOrderItemService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return stockOrderItemService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<StockOrderItemVO> page(@Valid StockOrderItemQueryDTO query) {
        return stockOrderItemService.pageQuery(query);
    }
}
