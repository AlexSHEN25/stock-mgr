package co.handk.backend.controller;

import co.handk.backend.entity.StockOrderItem;
import co.handk.backend.service.StockOrderItemService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stockOrderItem")
public class StockOrderItemController {

    @Autowired
    private StockOrderItemService stockOrderItemService;

    @PostMapping
    public Boolean create(@RequestBody StockOrderItem entity) {
        return stockOrderItemService.create(entity);
    }

    @GetMapping("/{id}")
    public StockOrderItem get(@PathVariable Long id) {
        return stockOrderItemService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody StockOrderItem entity) {
        return stockOrderItemService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return stockOrderItemService.delete(id);
    }

    @GetMapping("/list")
    public List<StockOrderItem> list() {
        return stockOrderItemService.listAll();
    }

    @GetMapping("/page")
    public PageResult<StockOrderItem> page(PageQuery query) {
        return stockOrderItemService.pageQuery(query);
    }
}
