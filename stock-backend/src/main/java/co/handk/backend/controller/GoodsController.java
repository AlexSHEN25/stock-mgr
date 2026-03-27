package co.handk.backend.controller;

import co.handk.backend.entity.Goods;
import co.handk.common.model.dto.GoodsDTO;
import co.handk.backend.service.GoodsService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid GoodsDTO dto) {
        return goodsService.create(dto);
    }

    @GetMapping("/{id}")
    public Goods get(@PathVariable @NotNull Long id) {
        return goodsService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid GoodsDTO dto) {
        return goodsService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return goodsService.delete(id);
    }

    @GetMapping("/list")
    public List<Goods> list() {
        return goodsService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Goods> page(@Valid PageQuery query) {
        return goodsService.pageQuery(query);
    }
}
