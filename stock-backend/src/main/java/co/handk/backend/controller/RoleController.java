package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.RoleService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateRoleDTO;
import co.handk.common.model.dto.query.RoleQueryDTO;
import co.handk.common.model.dto.update.UpdateRoleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateRoleDTO dto) {
        return roleService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public RoleVO get(@PathVariable("id") @NotNull Long id) {
        return roleService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateRoleDTO dto) {
        return roleService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return roleService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return roleService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<RoleVO> page(@Valid RoleQueryDTO query) {
        return roleService.page(query);
    }
}

