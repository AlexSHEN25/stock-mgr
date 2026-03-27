package co.handk.backend.controller;

import co.handk.backend.entity.UserRole;
import co.handk.common.model.dto.UserRoleDTO;
import co.handk.backend.service.UserRoleService;
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
@RequestMapping("/userRole")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid UserRoleDTO dto) {
        return userRoleService.create(dto);
    }

    @GetMapping("/{id}")
    public UserRole get(@PathVariable @NotNull Long id) {
        return userRoleService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UserRoleDTO dto) {
        return userRoleService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return userRoleService.delete(id);
    }

    @GetMapping("/list")
    public List<UserRole> list() {
        return userRoleService.listAll();
    }

    @GetMapping("/page")
    public PageResult<UserRole> page(@Valid PageQuery query) {
        return userRoleService.pageQuery(query);
    }
}
