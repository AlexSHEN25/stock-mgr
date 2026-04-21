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
        return userRoleService.create(dto);
    }
    @GetMapping("/{id}")
    public UserRoleVO get(@PathVariable @NotNull Long id) {
        return userRoleService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateUserRoleDTO dto) {
        return userRoleService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return userRoleService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<UserRoleVO> page(@Valid UserRoleQueryDTO query) {
        return userRoleService.pageQuery(query);
    }
}
