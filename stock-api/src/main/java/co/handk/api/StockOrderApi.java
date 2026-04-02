package co.handk.api;
import co.handk.common.model.vo.StockOrderVO;
import co.handk.common.model.dto.create.CreateStockOrderDTO;
import co.handk.common.model.dto.update.UpdateStockOrderDTO;
import co.handk.common.model.dto.query.StockOrderQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@Validated
@RequestMapping("/stockOrder")
public interface StockOrderApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateStockOrderDTO dto);
    @GetMapping("/{id}")
    StockOrderVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateStockOrderDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<StockOrderVO> page(@Valid StockOrderQueryDTO query);
}
