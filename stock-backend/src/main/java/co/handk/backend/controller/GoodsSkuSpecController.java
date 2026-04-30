package co.handk.backend.controller;

import co.handk.backend.service.GoodsSkuSpecService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsSkuSpecDTO;
import co.handk.common.model.dto.query.GoodsSkuSpecQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuSpecDTO;
import co.handk.common.model.vo.GoodsSkuSpecVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/goodsSkuSpec")
public class GoodsSkuSpecController {

    @Autowired
    private GoodsSkuSpecService goodsSkuSpecService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsSkuSpecDTO dto) {
        return goodsSkuSpecService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public GoodsSkuSpecVO get(@PathVariable("id") @NotNull Long id) {
        return goodsSkuSpecService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsSkuSpecDTO dto) {
        return goodsSkuSpecService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return goodsSkuSpecService.deleteByIdLogic(id) > 0;
    }

    @GetMapping("/page")
    public PageResult<GoodsSkuSpecVO> page(@Valid GoodsSkuSpecQueryDTO query) {
        return goodsSkuSpecService.page(query);
    }
}