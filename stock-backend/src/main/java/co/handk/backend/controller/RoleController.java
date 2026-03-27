package co.handk.backend.controller;

import co.handk.backend.entity.Role;
import co.handk.common.model.dto.RoleDTO;
import co.handk.backend.service.RoleService;
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
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid RoleDTO dto) {
        return roleService.create(dto);
    }

    @GetMapping("/{id}")
    public Role get(@PathVariable @NotNull Long id) {
        return roleService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid RoleDTO dto) {
        return roleService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return roleService.delete(id);
    }

    @GetMapping("/list")
    public List<Role> list() {
        return roleService.listAll();
    }

    @GetMapping("/page")
    public PageResult<Role> page(@Valid PageQuery query) {
        return roleService.pageQuery(query);
    }
}
