package co.handk.backend.controller;

import co.handk.backend.entity.RolePermission;
import co.handk.common.model.vo.RolePermissionVO;
import co.handk.common.model.dto.RolePermissionDTO;
import co.handk.backend.service.RolePermissionService;
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
@RequestMapping("/rolePermission")
public class RolePermissionController {

    @Autowired
    private RolePermissionService rolePermissionService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid RolePermissionDTO dto) {
        return rolePermissionService.create(dto);
    }

    @GetMapping("/{id}")
    public RolePermissionVO get(@PathVariable @NotNull Long id) {
        return rolePermissionService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid RolePermissionDTO dto) {
        return rolePermissionService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return rolePermissionService.delete(id);
    }

    @GetMapping("/list")
    public List<RolePermissionVO> list() {
        return rolePermissionService.listAll();
    }

    @GetMapping("/page")
    public PageResult<RolePermissionVO> page(@Valid PageQuery query) {
        return rolePermissionService.pageQuery(query);
    }
}
