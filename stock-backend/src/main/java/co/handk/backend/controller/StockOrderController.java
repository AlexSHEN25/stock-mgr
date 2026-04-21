package co.handk.backend.controller;

import co.handk.backend.service.StockOrderService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockOrderDTO;
import co.handk.common.model.dto.query.StockOrderQueryDTO;
import co.handk.common.model.dto.update.UpdateStockOrderDTO;
import co.handk.common.model.vo.StockOrderVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/stockOrder")
public class StockOrderController {
    @Autowired
    private StockOrderService stockOrderService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockOrderDTO dto) {
        return stockOrderService.create(dto);
    }
    @GetMapping("/{id}")
    public StockOrderVO get(@PathVariable @NotNull Long id) {
        return stockOrderService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockOrderDTO dto) {
        return stockOrderService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return stockOrderService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<StockOrderVO> page(@Valid StockOrderQueryDTO query) {
        return stockOrderService.pageQuery(query);
    }
}
