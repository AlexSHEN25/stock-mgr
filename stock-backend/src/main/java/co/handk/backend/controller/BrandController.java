package co.handk.backend.controller;

import co.handk.backend.entity.Brand;
import co.handk.backend.service.BrandService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @PostMapping
    public Boolean create(@RequestBody Brand entity) {
        return brandService.create(entity);
    }

    @GetMapping("/{id}")
    public Brand get(@PathVariable Long id) {
        return brandService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Brand entity) {
        return brandService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return brandService.delete(id);
    }

    @GetMapping("/list")
    public List<Brand> list() {
        return brandService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Brand> page(PageQuery query) {
        return brandService.pageQuery(query);
    }
}
