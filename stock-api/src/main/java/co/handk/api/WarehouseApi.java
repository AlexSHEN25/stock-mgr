package co.handk.api;
import co.handk.common.model.vo.WarehouseVO;
import co.handk.common.model.dto.create.CreateWarehouseDTO;
import co.handk.common.model.dto.update.UpdateWarehouseDTO;
import co.handk.common.model.dto.query.WarehouseQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@Validated
@RequestMapping("/warehouse")
public interface WarehouseApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateWarehouseDTO dto);
    @GetMapping("/{id}")
    WarehouseVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateWarehouseDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<WarehouseVO> page(@Valid WarehouseQueryDTO query);
}
