package co.handk.backend.controller;

import co.handk.backend.service.PriceRecordService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreatePriceRecordDTO;
import co.handk.common.model.dto.query.PriceRecordQueryDTO;
import co.handk.common.model.dto.update.UpdatePriceRecordDTO;
import co.handk.common.model.vo.PriceRecordVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/priceRecord")
public class PriceRecordController {
    @Autowired
    private PriceRecordService priceRecordService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreatePriceRecordDTO dto) {
        return priceRecordService.create(dto);
    }
    @GetMapping("/{id}")
    public PriceRecordVO get(@PathVariable @NotNull Long id) {
        return priceRecordService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdatePriceRecordDTO dto) {
        return priceRecordService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return priceRecordService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<PriceRecordVO> page(@Valid PriceRecordQueryDTO query) {
        return priceRecordService.pageQuery(query);
    }
}
