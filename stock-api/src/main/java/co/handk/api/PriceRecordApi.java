package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreatePriceRecordDTO;
import co.handk.common.model.dto.query.PriceRecordQueryDTO;
import co.handk.common.model.dto.update.UpdatePriceRecordDTO;
import co.handk.common.model.vo.PriceRecordVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/priceRecord")
public interface PriceRecordApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreatePriceRecordDTO dto);
    @GetMapping("/{id}")
    PriceRecordVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdatePriceRecordDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<PriceRecordVO> page(@Valid PriceRecordQueryDTO query);
}
