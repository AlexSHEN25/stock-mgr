package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateUserRoleDTO;
import co.handk.common.model.dto.query.UserRoleQueryDTO;
import co.handk.common.model.dto.update.UpdateUserRoleDTO;
import co.handk.common.model.vo.UserRoleVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/userRole")
public interface UserRoleApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateUserRoleDTO dto);
    @GetMapping("/{id}")
    UserRoleVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateUserRoleDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<UserRoleVO> page(@Valid UserRoleQueryDTO query);
}
