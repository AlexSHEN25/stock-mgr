package co.handk.backend.controller;

import co.handk.backend.service.RoleService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRoleDTO;
import co.handk.common.model.dto.query.RoleQueryDTO;
import co.handk.common.model.dto.update.UpdateRoleDTO;
import co.handk.common.model.vo.RoleVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/role")
public class RoleController {
    @Autowired
    private RoleService roleService;
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
        return roleService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<RoleVO> page(@Valid RoleQueryDTO query) {
        return roleService.page(query);
    }
}

