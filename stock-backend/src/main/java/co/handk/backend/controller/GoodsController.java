package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.backend.service.GoodsService;
import co.handk.common.constant.NumberConstant;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsDTO;
import co.handk.common.model.dto.query.GoodsQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsDTO;
import co.handk.common.model.vo.GoodsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/goods")
public class GoodsController {
    private final GoodsService goodsService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateGoodsDTO dto) {
        return goodsService.saveGoods(dto);
    }

    @GetMapping("/{id}")
    public GoodsVO get(@PathVariable("id") @NotNull Long id) {
        return goodsService.getGoodsById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateGoodsDTO dto) {
        return goodsService.updateGoods(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return goodsService.deleteGoodsById(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return goodsService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<GoodsVO> page(@Valid GoodsQueryDTO query) {
        return goodsService.pageGoods(query);
    }


}
