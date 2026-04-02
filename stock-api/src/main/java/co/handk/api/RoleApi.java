package co.handk.api;
import co.handk.common.model.vo.RoleVO;
import co.handk.common.model.dto.create.CreateRoleDTO;
import co.handk.common.model.dto.update.UpdateRoleDTO;
import co.handk.common.model.dto.query.RoleQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@Validated
@RequestMapping("/role")
public interface RoleApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateRoleDTO dto);
    @GetMapping("/{id}")
    RoleVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateRoleDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<RoleVO> page(@Valid RoleQueryDTO query);
}
