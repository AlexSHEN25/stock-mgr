package co.handk.backend.controller;
import co.handk.common.model.vo.StockRecordVO;
import co.handk.common.model.dto.create.CreateStockRecordDTO;
import co.handk.common.model.dto.update.UpdateStockRecordDTO;
import co.handk.backend.service.StockRecordService;
import co.handk.common.model.dto.query.StockRecordQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/stockRecord")
public class StockRecordController {
    @Autowired
    private StockRecordService stockRecordService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockRecordDTO dto) {
        return stockRecordService.create(dto);
    }
    @GetMapping("/{id}")
    public StockRecordVO get(@PathVariable @NotNull Long id) {
        return stockRecordService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockRecordDTO dto) {
        return stockRecordService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return stockRecordService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<StockRecordVO> page(@Valid StockRecordQueryDTO query) {
        return stockRecordService.pageQuery(query);
    }
}
