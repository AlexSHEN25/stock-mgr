package co.handk.backend.controller;

import co.handk.backend.service.StockService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockDTO;
import co.handk.common.model.dto.query.StockQueryDTO;
import co.handk.common.model.dto.update.UpdateStockDTO;
import co.handk.common.model.vo.StockVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockDTO dto) {
        return stockService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public StockVO get(@PathVariable("id") @NotNull Long id) {
        return stockService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockDTO dto) {
        return stockService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return stockService.deleteByIdLogic(id) > 0;
    }

    @GetMapping("/page")
    public PageResult<StockVO> page(@Valid StockQueryDTO query) {
        return stockService.page(query);
    }
}
