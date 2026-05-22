package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.GoodsLevelPriceService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateGoodsLevelPriceDTO;
import co.handk.common.model.dto.query.GoodsLevelPriceQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsLevelPriceDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/goodsLevelPrice")
@RequiredArgsConstructor
public class GoodsLevelPriceController {
    private final GoodsLevelPriceService goodsLevelPriceService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsLevelPriceDTO dto) {
        return goodsLevelPriceService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public GoodsLevelPriceVO get(@PathVariable("id") @NotNull Long id) {
        return goodsLevelPriceService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsLevelPriceDTO dto) {
        return goodsLevelPriceService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return goodsLevelPriceService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return goodsLevelPriceService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<GoodsLevelPriceVO> page(@Valid GoodsLevelPriceQueryDTO query) {
        return goodsLevelPriceService.page(query);
    }
}

