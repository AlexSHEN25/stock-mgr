package co.handk.backend.controller;

import co.handk.backend.entity.GoodsType;
import co.handk.backend.service.GoodsTypeService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goodsType")
public class GoodsTypeController {

    @Autowired
    private GoodsTypeService goodsTypeService;

    @PostMapping
    public Boolean create(@RequestBody GoodsType entity) {
        return goodsTypeService.create(entity);
    }

    @GetMapping("/{id}")
    public GoodsType get(@PathVariable Long id) {
        return goodsTypeService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody GoodsType entity) {
        return goodsTypeService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return goodsTypeService.delete(id);
    }

    @GetMapping("/list")
    public List<GoodsType> list() {
        return goodsTypeService.listAll();
    }

    @GetMapping("/page")
    public PageResult<GoodsType> page(PageQuery query) {
        return goodsTypeService.pageQuery(query);
    }
}
