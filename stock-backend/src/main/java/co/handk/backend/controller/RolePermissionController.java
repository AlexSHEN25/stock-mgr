package co.handk.backend.controller;

import co.handk.api.RolePermissionApi;
import co.handk.common.model.vo.RolePermissionVO;
import co.handk.common.model.dto.create.CreateRolePermissionDTO;
import co.handk.common.model.dto.update.UpdateRolePermissionDTO;
import co.handk.backend.service.RolePermissionService;
import co.handk.common.model.dto.query.RolePermissionQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/rolePermission")
public class RolePermissionController implements RolePermissionApi {
    @Autowired
    private RolePermissionService rolePermissionService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRolePermissionDTO dto) {
        return rolePermissionService.create(dto);
    }
    @GetMapping("/{id}")
    public RolePermissionVO get(@PathVariable @NotNull Long id) {
        return rolePermissionService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRolePermissionDTO dto) {
        return rolePermissionService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return rolePermissionService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<RolePermissionVO> page(@Valid RolePermissionQueryDTO query) {
        return rolePermissionService.pageQuery(query);
    }
}
