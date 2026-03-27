package co.handk.backend.controller;

import co.handk.backend.entity.StockOrderItem;
import co.handk.common.model.dto.StockOrderItemDTO;
import co.handk.backend.service.StockOrderItemService;
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
@RequestMapping("/stockOrderItem")
public class StockOrderItemController {

    @Autowired
    private StockOrderItemService stockOrderItemService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid StockOrderItemDTO dto) {
        return stockOrderItemService.create(dto);
    }

    @GetMapping("/{id}")
    public StockOrderItem get(@PathVariable @NotNull Long id) {
        return stockOrderItemService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid StockOrderItemDTO dto) {
        return stockOrderItemService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return stockOrderItemService.delete(id);
    }

    @GetMapping("/list")
    public List<StockOrderItem> list() {
        return stockOrderItemService.listAll();
    }

    @GetMapping("/page")
    public PageResult<StockOrderItem> page(@Valid PageQuery query) {
        return stockOrderItemService.pageQuery(query);
    }
}
