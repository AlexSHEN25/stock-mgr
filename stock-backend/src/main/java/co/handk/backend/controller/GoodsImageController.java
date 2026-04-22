package co.handk.backend.controller;

import co.handk.backend.service.GoodsImageService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsImageDTO;
import co.handk.common.model.dto.query.GoodsImageQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsImageDTO;
import co.handk.common.model.vo.GoodsImageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/goodsImage")
public class GoodsImageController {
    @Autowired
    private GoodsImageService goodsImageService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsImageDTO dto) {
        return goodsImageService.create(dto);
    }

    @GetMapping("/{id}")
    public GoodsImageVO get(@PathVariable @NotNull Long id) {
        return goodsImageService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsImageDTO dto) {
        return goodsImageService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return goodsImageService.delete(id);
    }

    @GetMapping("/page")
    public PageResult<GoodsImageVO> page(@Valid GoodsImageQueryDTO query) {
        return goodsImageService.pageQuery(query);
    }
}
