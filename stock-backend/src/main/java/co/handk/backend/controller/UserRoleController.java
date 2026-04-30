package co.handk.backend.controller;

import co.handk.backend.service.UserRoleService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateUserRoleDTO;
import co.handk.common.model.dto.query.UserRoleQueryDTO;
import co.handk.common.model.dto.update.UpdateUserRoleDTO;
import co.handk.common.model.vo.UserRoleVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/userRole")
public class UserRoleController {
    @Autowired
    private UserRoleService userRoleService;
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
        return userRoleService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<UserRoleVO> page(@Valid UserRoleQueryDTO query) {
        return userRoleService.page(query);
    }
}

