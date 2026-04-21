package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateRolePermissionDTO;
import co.handk.common.model.dto.query.RolePermissionQueryDTO;
import co.handk.common.model.dto.update.UpdateRolePermissionDTO;
import co.handk.common.model.vo.RolePermissionVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/rolePermission")
public interface RolePermissionApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateRolePermissionDTO dto);
    @GetMapping("/{id}")
    RolePermissionVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateRolePermissionDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<RolePermissionVO> page(@Valid RolePermissionQueryDTO query);
}
