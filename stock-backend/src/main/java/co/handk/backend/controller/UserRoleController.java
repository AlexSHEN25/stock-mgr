package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.UserRoleService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateUserRoleDTO;
import co.handk.common.model.dto.query.UserRoleQueryDTO;
import co.handk.common.model.dto.update.UpdateUserRoleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/userRole")
@RequiredArgsConstructor
public class UserRoleController {
    private final UserRoleService userRoleService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateUserRoleDTO dto) {
        return userRoleService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public UserRoleVO get(@PathVariable("id") @NotNull Long id) {
        return userRoleService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateUserRoleDTO dto) {
        return userRoleService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return userRoleService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return userRoleService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<UserRoleVO> page(@Valid UserRoleQueryDTO query) {
        return userRoleService.page(query);
    }
}

