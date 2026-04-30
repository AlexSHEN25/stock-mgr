package co.handk.backend.controller;

import co.handk.backend.service.StockTypeService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateStockTypeDTO;
import co.handk.common.model.dto.query.GoodsTypeQueryDTO;
import co.handk.common.model.dto.update.UpdateStockTypeDTO;
import co.handk.common.model.vo.StockTypeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/goodsType")
public class GoodsTypeController {
    @Autowired
    private StockTypeService goodsTypeService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateStockTypeDTO dto) {
        return goodsTypeService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public StockTypeVO get(@PathVariable("id") @NotNull Long id) {
        return goodsTypeService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateStockTypeDTO dto) {
        return goodsTypeService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return goodsTypeService.deleteByIdLogic(id) > 0;
    }

    @GetMapping("/page")
    public PageResult<StockTypeVO> page(@Valid GoodsTypeQueryDTO query) {
        return goodsTypeService.page(query);
    }
}

