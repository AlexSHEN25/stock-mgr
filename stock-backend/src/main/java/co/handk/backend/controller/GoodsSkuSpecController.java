package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.GoodsSkuSpecService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateGoodsSkuSpecDTO;
import co.handk.common.model.dto.query.GoodsSkuSpecQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsSkuSpecDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/goodsSkuSpec")
@RequiredArgsConstructor
public class GoodsSkuSpecController {
    private final GoodsSkuSpecService goodsSkuSpecService;

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
        return goodsSkuSpecService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return goodsSkuSpecService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<GoodsSkuSpecVO> page(@Valid GoodsSkuSpecQueryDTO query) {
        return goodsSkuSpecService.page(query);
    }
}
