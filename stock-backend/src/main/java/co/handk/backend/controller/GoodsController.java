package co.handk.backend.controller;
import co.handk.common.model.vo.GoodsVO;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.backend.service.GoodsService;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsDTO dto) {
        return goodsService.create(dto);
    }
    @GetMapping("/{id}")
    public GoodsVO get(@PathVariable @NotNull Long id) {
        return goodsService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsDTO dto) {
        return goodsService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return goodsService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<GoodsVO> page(@Valid GoodsQueryDTO query) {
        return goodsService.pageQuery(query);
    }
}
