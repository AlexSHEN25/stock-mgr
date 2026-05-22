package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.PermissionService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreatePermissionDTO;
import co.handk.common.model.dto.query.PermissionQueryDTO;
import co.handk.common.model.dto.update.UpdatePermissionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreatePermissionDTO dto) {
        return permissionService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public PermissionVO get(@PathVariable("id") @NotNull Long id) {
        return permissionService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdatePermissionDTO dto) {
        return permissionService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return permissionService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return permissionService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<PermissionVO> page(@Valid PermissionQueryDTO query) {
        return permissionService.page(query);
    }
}

