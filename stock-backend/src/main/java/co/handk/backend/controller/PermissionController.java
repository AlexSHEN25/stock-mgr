package co.handk.backend.controller;

import co.handk.common.model.vo.PermissionVO;
import co.handk.common.model.dto.create.CreatePermissionDTO;
import co.handk.common.model.dto.update.UpdatePermissionDTO;
import co.handk.backend.service.PermissionService;
import co.handk.common.model.dto.query.PermissionQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/permission")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreatePermissionDTO dto) {
        return permissionService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public PermissionVO get(@PathVariable @NotNull Long id) {
        return permissionService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdatePermissionDTO dto) {
        return permissionService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return permissionService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<PermissionVO> page(@Valid PermissionQueryDTO query) {
        return permissionService.page(query);
    }
}

