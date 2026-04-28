package co.handk.backend.controller;

import co.handk.backend.service.StockTypeService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockTypeDTO;
import co.handk.common.model.dto.query.GoodsTypeQueryDTO;
import co.handk.common.model.dto.update.UpdateStockTypeDTO;
import co.handk.common.model.vo.StockTypeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/stockType")
public class StockTypeController {

    private final StockTypeService stockTypeService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockTypeDTO dto) {
        return stockTypeService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public StockTypeVO get(@PathVariable @NotNull Long id) {
        return stockTypeService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockTypeDTO dto) {
        return stockTypeService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return stockTypeService.deleteByIdLogic(id) > 0;
    }

    @GetMapping("/page")
    public PageResult<StockTypeVO> page(@Valid GoodsTypeQueryDTO query) {
        return stockTypeService.page(query);
    }
}

