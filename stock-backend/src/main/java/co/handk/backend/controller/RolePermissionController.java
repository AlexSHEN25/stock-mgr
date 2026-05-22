package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.RolePermissionService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateRolePermissionDTO;
import co.handk.common.model.dto.query.RolePermissionQueryDTO;
import co.handk.common.model.dto.update.UpdateRolePermissionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/rolePermission")
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;

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
        return rolePermissionService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return rolePermissionService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<RolePermissionVO> page(@Valid RolePermissionQueryDTO query) {
        return rolePermissionService.page(query);
    }
}

