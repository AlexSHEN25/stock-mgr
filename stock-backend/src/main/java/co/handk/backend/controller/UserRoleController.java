package co.handk.backend.controller;

import co.handk.backend.entity.UserRole;
import co.handk.backend.service.UserRoleService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/userRole")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @PostMapping
    public Boolean create(@RequestBody UserRole entity) {
        return userRoleService.create(entity);
    }

    @GetMapping("/{id}")
    public UserRole get(@PathVariable Long id) {
        return userRoleService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody UserRole entity) {
        return userRoleService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return userRoleService.delete(id);
    }

    @GetMapping("/list")
    public List<UserRole> list() {
        return userRoleService.listAll();
    }

    @GetMapping("/page")
    public PageResult<UserRole> page(PageQuery query) {
        return userRoleService.pageQuery(query);
    }
}
