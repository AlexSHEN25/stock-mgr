package co.handk.backend.controller;
import co.handk.common.model.vo.GoodsTypeVO;
import co.handk.common.model.dto.create.CreateGoodsTypeDTO;
import co.handk.common.model.dto.update.UpdateGoodsTypeDTO;
import co.handk.backend.service.GoodsTypeService;
import co.handk.common.model.dto.query.GoodsTypeQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/goodsType")
public class GoodsTypeController {
    @Autowired
    private GoodsTypeService goodsTypeService;
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
