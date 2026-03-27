package co.handk.backend.controller;

import co.handk.backend.entity.Goods;
import co.handk.backend.service.GoodsService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping
    public Boolean create(@RequestBody Goods entity) {
        return goodsService.create(entity);
    }

    @GetMapping("/{id}")
    public Goods get(@PathVariable Long id) {
        return goodsService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Goods entity) {
        return goodsService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return goodsService.delete(id);
    }

    @GetMapping("/list")
    public List<Goods> list() {
        return goodsService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Goods> page(PageQuery query) {
        return goodsService.pageQuery(query);
    }
}
