package co.handk.backend.controller;

import co.handk.backend.service.RolePermissionService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRolePermissionDTO;
import co.handk.common.model.dto.query.RolePermissionQueryDTO;
import co.handk.common.model.dto.update.UpdateRolePermissionDTO;
import co.handk.common.model.vo.RolePermissionVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/rolePermission")
public class RolePermissionController {
    @Autowired
    private RolePermissionService rolePermissionService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRolePermissionDTO dto) {
        return rolePermissionService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public RolePermissionVO get(@PathVariable("id") @NotNull Long id) {
        return rolePermissionService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRolePermissionDTO dto) {
        return rolePermissionService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return rolePermissionService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<RolePermissionVO> page(@Valid RolePermissionQueryDTO query) {
        return rolePermissionService.page(query);
    }
}

