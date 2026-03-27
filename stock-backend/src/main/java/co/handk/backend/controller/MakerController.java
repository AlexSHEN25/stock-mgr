package co.handk.backend.controller;

import co.handk.backend.entity.Maker;
import co.handk.backend.service.MakerService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maker")
public class MakerController {

    @Autowired
    private MakerService makerService;

    @PostMapping
    public Boolean create(@RequestBody Maker entity) {
        return makerService.create(entity);
    }

    @GetMapping("/{id}")
    public Maker get(@PathVariable Long id) {
        return makerService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Maker entity) {
        return makerService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return makerService.delete(id);
    }

    @GetMapping("/list")
    public List<Maker> list() {
        return makerService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Maker> page(PageQuery query) {
        return makerService.pageQuery(query);
    }
}
