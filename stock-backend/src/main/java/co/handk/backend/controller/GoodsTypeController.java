package co.handk.backend.controller;

import co.handk.backend.service.StockTypeService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsTypeDTO;
import co.handk.common.model.dto.query.GoodsTypeQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsTypeDTO;
import co.handk.common.model.vo.GoodsTypeVO;
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
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsTypeDTO dto) {
        return goodsTypeService.create(dto);
    }

    @GetMapping("/{id}")
    public GoodsTypeVO get(@PathVariable @NotNull Long id) {
        return goodsTypeService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsTypeDTO dto) {
        return goodsTypeService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return goodsTypeService.delete(id);
    }

    @GetMapping("/page")
    public PageResult<GoodsTypeVO> page(@Valid GoodsTypeQueryDTO query) {
        return goodsTypeService.pageQuery(query);
    }
}
