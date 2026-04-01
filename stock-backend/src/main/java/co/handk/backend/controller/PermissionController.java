package co.handk.backend.controller;

import co.handk.backend.entity.Permission;
import co.handk.common.model.vo.PermissionVO;
import co.handk.common.model.dto.PermissionDTO;
import co.handk.backend.service.PermissionService;
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
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid PermissionDTO dto) {
        return permissionService.create(dto);
    }

    @GetMapping("/{id}")
    public PermissionVO get(@PathVariable @NotNull Long id) {
        return permissionService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid PermissionDTO dto) {
        return permissionService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return permissionService.delete(id);
    }

    @GetMapping("/list")
    public List<PermissionVO> list() {
        return permissionService.listAll();
    }

    @GetMapping("/page")
    public PageResult<PermissionVO> page(@Valid PageQuery query) {
        return permissionService.pageQuery(query);
    }
}
