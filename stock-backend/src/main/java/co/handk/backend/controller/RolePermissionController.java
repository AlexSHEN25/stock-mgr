package co.handk.backend.controller;

import co.handk.backend.entity.RolePermission;
import co.handk.backend.service.RolePermissionService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rolePermission")
public class RolePermissionController {

    @Autowired
    private RolePermissionService rolePermissionService;

    @PostMapping
    public Boolean create(@RequestBody RolePermission entity) {
        return rolePermissionService.create(entity);
    }

    @GetMapping("/{id}")
    public RolePermission get(@PathVariable Long id) {
        return rolePermissionService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody RolePermission entity) {
        return rolePermissionService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return rolePermissionService.delete(id);
    }

    @GetMapping("/list")
    public List<RolePermission> list() {
        return rolePermissionService.listAll();
    }

    @GetMapping("/page")
    public PageResult<RolePermission> page(PageQuery query) {
        return rolePermissionService.pageQuery(query);
    }
}
