package co.handk.backend.controller;

import co.handk.backend.entity.PriceRecord;
import co.handk.common.model.dto.PriceRecordDTO;
import co.handk.backend.service.PriceRecordService;
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
@RequestMapping("/priceRecord")
public class PriceRecordController {

    @Autowired
    private PriceRecordService priceRecordService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid PriceRecordDTO dto) {
        return priceRecordService.create(dto);
    }

    @GetMapping("/{id}")
    public PriceRecord get(@PathVariable @NotNull Long id) {
        return priceRecordService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid PriceRecordDTO dto) {
        return priceRecordService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return priceRecordService.delete(id);
    }

    @GetMapping("/list")
    public List<PriceRecord> list() {
        return priceRecordService.listAll();
    }

    @GetMapping("/page")
    public PageResult<PriceRecord> page(@Valid PageQuery query) {
        return priceRecordService.pageQuery(query);
    }
}
