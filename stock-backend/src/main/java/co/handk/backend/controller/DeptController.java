package co.handk.backend.controller;

import co.handk.backend.entity.Dept;
import co.handk.backend.service.DeptService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dept")
public class DeptController {

    @Autowired
    private DeptService deptService;

    @PostMapping
    public Boolean create(@RequestBody Dept entity) {
        return deptService.create(entity);
    }

    @GetMapping("/{id}")
    public Dept get(@PathVariable Long id) {
        return deptService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Dept entity) {
        return deptService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return deptService.delete(id);
    }

    @GetMapping("/list")
    public List<Dept> list() {
        return deptService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Dept> page(PageQuery query) {
        return deptService.pageQuery(query);
    }
}
