package co.handk.backend.controller;

import co.handk.backend.entity.Permission;
import co.handk.backend.service.PermissionService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping
    public Boolean create(@RequestBody Permission entity) {
        return permissionService.create(entity);
    }

    @GetMapping("/{id}")
    public Permission get(@PathVariable Long id) {
        return permissionService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody Permission entity) {
        return permissionService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return permissionService.delete(id);
    }

    @GetMapping("/list")
    public List<Permission> list() {
        return permissionService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Permission> page(PageQuery query) {
        return permissionService.pageQuery(query);
    }
}
