package co.handk.api;
import co.handk.common.model.vo.StockRecordVO;
import co.handk.common.model.dto.create.CreateStockRecordDTO;
import co.handk.common.model.dto.update.UpdateStockRecordDTO;
import co.handk.common.model.dto.query.StockRecordQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@Validated
@RequestMapping("/stockRecord")
public interface StockRecordApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateStockRecordDTO dto);
    @GetMapping("/{id}")
    StockRecordVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateStockRecordDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<StockRecordVO> page(@Valid StockRecordQueryDTO query);
}
