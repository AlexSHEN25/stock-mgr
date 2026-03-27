package co.handk.backend.controller;

import co.handk.backend.entity.StockRecord;
import co.handk.backend.service.StockRecordService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stockRecord")
public class StockRecordController {

    @Autowired
    private StockRecordService stockRecordService;

    @PostMapping
    public Boolean create(@RequestBody StockRecord entity) {
        return stockRecordService.create(entity);
    }

    @GetMapping("/{id}")
    public StockRecord get(@PathVariable Long id) {
        return stockRecordService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody StockRecord entity) {
        return stockRecordService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return stockRecordService.delete(id);
    }

    @GetMapping("/list")
    public List<StockRecord> list() {
        return stockRecordService.listAll();
    }

    @GetMapping("/page")
    public PageResult<StockRecord> page(PageQuery query) {
        return stockRecordService.pageQuery(query);
    }
}
