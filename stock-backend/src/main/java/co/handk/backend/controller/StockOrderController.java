package co.handk.backend.controller;

import co.handk.backend.entity.StockOrder;
import co.handk.common.model.dto.StockOrderDTO;
import co.handk.backend.service.StockOrderService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/stockOrder")
public class StockOrderController {

    @Autowired
    private StockOrderService stockOrderService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid StockOrderDTO dto) {
        return stockOrderService.create(dto);
    }

    @GetMapping("/{id}")
    public StockOrder get(@PathVariable @NotNull Long id) {
        return stockOrderService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid StockOrderDTO dto) {
        return stockOrderService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return stockOrderService.delete(id);
    }

    @GetMapping("/list")
    public List<StockOrder> list() {
        return stockOrderService.listAll();
    }

    @GetMapping("/page")
    public PageResult<StockOrder> page(@Valid PageQuery query) {
        return stockOrderService.pageQuery(query);
    }
}
