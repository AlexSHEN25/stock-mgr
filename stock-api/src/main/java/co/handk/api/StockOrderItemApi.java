package co.handk.api;
import co.handk.common.model.vo.StockOrderItemVO;
import co.handk.common.model.dto.create.CreateStockOrderItemDTO;
import co.handk.common.model.dto.update.UpdateStockOrderItemDTO;
import co.handk.common.model.dto.query.StockOrderItemQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@Validated
@RequestMapping("/stockOrderItem")
public interface StockOrderItemApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateStockOrderItemDTO dto);
    @GetMapping("/{id}")
    StockOrderItemVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateStockOrderItemDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<StockOrderItemVO> page(@Valid StockOrderItemQueryDTO query);
}
