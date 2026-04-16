package co.handk.backend.controller;

import co.handk.common.model.vo.RoleVO;
import co.handk.common.model.dto.create.CreateRoleDTO;
import co.handk.common.model.dto.update.UpdateRoleDTO;
import co.handk.backend.service.RoleService;
import co.handk.common.model.dto.query.RoleQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/role")
public class RoleController {
    @Autowired
    private RoleService roleService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRoleDTO dto) {
        return roleService.create(dto);
    }
    @GetMapping("/{id}")
    public RoleVO get(@PathVariable @NotNull Long id) {
        return roleService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRoleDTO dto) {
        return roleService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return roleService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<RoleVO> page(@Valid RoleQueryDTO query) {
        return roleService.pageQuery(query);
    }
}
