package co.handk.backend.controller;

import co.handk.backend.service.GoodsSkuService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsSkuDTO;
import co.handk.common.model.dto.query.GoodsSkuQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuDTO;
import co.handk.common.model.vo.GoodsSkuVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/goodsSku")
public class GoodsSkuController {
    @Autowired
    private GoodsSkuService goodsSkuService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsSkuDTO dto) {
        return goodsSkuService.create(dto);
    }

    @GetMapping("/{id}")
    public GoodsSkuVO get(@PathVariable @NotNull Long id) {
        return goodsSkuService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsSkuDTO dto) {
        return goodsSkuService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return goodsSkuService.delete(id);
    }

    @GetMapping("/page")
    public PageResult<GoodsSkuVO> page(@Valid GoodsSkuQueryDTO query) {
        return goodsSkuService.pageQuery(query);
    }
}
