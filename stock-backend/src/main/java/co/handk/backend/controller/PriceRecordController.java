package co.handk.backend.controller;

import co.handk.backend.entity.PriceRecord;
import co.handk.backend.service.PriceRecordService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/priceRecord")
public class PriceRecordController {

    @Autowired
    private PriceRecordService priceRecordService;

    @PostMapping
    public Boolean create(@RequestBody PriceRecord entity) {
        return priceRecordService.create(entity);
    }

    @GetMapping("/{id}")
    public PriceRecord get(@PathVariable Long id) {
        return priceRecordService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody PriceRecord entity) {
        return priceRecordService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return priceRecordService.delete(id);
    }

    @GetMapping("/list")
    public List<PriceRecord> list() {
        return priceRecordService.listAll();
    }

    @GetMapping("/page")
    public PageResult<PriceRecord> page(PageQuery query) {
        return priceRecordService.pageQuery(query);
    }
}
