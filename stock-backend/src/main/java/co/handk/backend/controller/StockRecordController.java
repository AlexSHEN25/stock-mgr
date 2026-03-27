package co.handk.backend.controller;

import co.handk.backend.entity.StockRecord;
import co.handk.common.model.dto.StockRecordDTO;
import co.handk.backend.service.StockRecordService;
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
@RequestMapping("/stockRecord")
public class StockRecordController {

    @Autowired
    private StockRecordService stockRecordService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid StockRecordDTO dto) {
        return stockRecordService.create(dto);
    }

    @GetMapping("/{id}")
    public StockRecord get(@PathVariable @NotNull Long id) {
        return stockRecordService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid StockRecordDTO dto) {
        return stockRecordService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return stockRecordService.delete(id);
    }

    @GetMapping("/list")
    public List<StockRecord> list() {
        return stockRecordService.listAll();
    }

    @GetMapping("/page")
    public PageResult<StockRecord> page(@Valid PageQuery query) {
        return stockRecordService.pageQuery(query);
    }
}
