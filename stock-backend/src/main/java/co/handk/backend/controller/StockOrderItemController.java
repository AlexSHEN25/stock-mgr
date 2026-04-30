package co.handk.backend.controller;

import co.handk.backend.service.StockOrderItemService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockOrderItemDTO;
import co.handk.common.model.dto.query.StockOrderItemQueryDTO;
import co.handk.common.model.dto.update.UpdateStockOrderItemDTO;
import co.handk.common.model.vo.StockOrderItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/stockOrderItem")
public class StockOrderItemController {
    @Autowired
    private StockOrderItemService stockOrderItemService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockOrderItemDTO dto) {
        return stockOrderItemService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public StockOrderItemVO get(@PathVariable("id") @NotNull Long id) {
        return stockOrderItemService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockOrderItemDTO dto) {
        return stockOrderItemService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return stockOrderItemService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<StockOrderItemVO> page(@Valid StockOrderItemQueryDTO query) {
        return stockOrderItemService.page(query);
    }
}

