package co.handk.backend.controller;

import co.handk.backend.entity.GoodsType;
import co.handk.common.model.vo.GoodsTypeVO;
import co.handk.common.model.dto.GoodsTypeDTO;
import co.handk.backend.service.GoodsTypeService;
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
@RequestMapping("/goodsType")
public class GoodsTypeController {

    @Autowired
    private GoodsTypeService goodsTypeService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid GoodsTypeDTO dto) {
        return goodsTypeService.create(dto);
    }

    @GetMapping("/{id}")
    public GoodsTypeVO get(@PathVariable @NotNull Long id) {
        return goodsTypeService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid GoodsTypeDTO dto) {
        return goodsTypeService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return goodsTypeService.delete(id);
    }

    @GetMapping("/list")
    public List<GoodsTypeVO> list() {
        return goodsTypeService.listAll();
    }

    @GetMapping("/page")
    public PageResult<GoodsTypeVO> page(@Valid PageQuery query) {
        return goodsTypeService.pageQuery(query);
    }
}
