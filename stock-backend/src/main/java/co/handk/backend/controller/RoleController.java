package co.handk.backend.controller;

import co.handk.backend.entity.Role;
import co.handk.backend.service.RoleService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping
    public Boolean create(@RequestBody Role entity) {
        return roleService.create(entity);
    }

    @GetMapping("/{id}")
    public Role get(@PathVariable Long id) {
        return roleService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Role entity) {
        return roleService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return roleService.delete(id);
    }

    @GetMapping("/list")
    public List<Role> list() {
        return roleService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Role> page(PageQuery query) {
        return roleService.pageQuery(query);
    }
}
