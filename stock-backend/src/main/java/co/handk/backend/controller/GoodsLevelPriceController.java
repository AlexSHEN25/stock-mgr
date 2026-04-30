package co.handk.backend.controller;

import co.handk.backend.service.GoodsLevelPriceService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateGoodsLevelPriceDTO;
import co.handk.common.model.dto.query.GoodsLevelPriceQueryDTO;
import co.handk.common.model.dto.update.UpdateGoodsLevelPriceDTO;
import co.handk.common.model.vo.GoodsLevelPriceVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/goodsLevelPrice")
public class GoodsLevelPriceController {
    @Autowired
    private GoodsLevelPriceService goodsLevelPriceService;

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
        return goodsLevelPriceService.deleteByIdLogic(id) > 0;
    }

    @GetMapping("/page")
    public PageResult<GoodsLevelPriceVO> page(@Valid GoodsLevelPriceQueryDTO query) {
        return goodsLevelPriceService.page(query);
    }
}

