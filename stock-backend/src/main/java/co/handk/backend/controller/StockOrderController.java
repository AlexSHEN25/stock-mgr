package co.handk.backend.controller;

import co.handk.backend.entity.StockOrder;
import co.handk.backend.service.StockOrderService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stockOrder")
public class StockOrderController {

    @Autowired
    private StockOrderService stockOrderService;

    @PostMapping
    public Boolean create(@RequestBody StockOrder entity) {
        return stockOrderService.create(entity);
    }

    @GetMapping("/{id}")
    public StockOrder get(@PathVariable Long id) {
        return stockOrderService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody StockOrder entity) {
        return stockOrderService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return stockOrderService.delete(id);
    }

    @GetMapping("/list")
    public List<StockOrder> list() {
        return stockOrderService.listAll();
    }

    @GetMapping("/page")
    public PageResult<StockOrder> page(PageQuery query) {
        return stockOrderService.pageQuery(query);
    }
}
