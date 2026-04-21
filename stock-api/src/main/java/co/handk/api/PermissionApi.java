package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreatePermissionDTO;
import co.handk.common.model.dto.query.PermissionQueryDTO;
import co.handk.common.model.dto.update.UpdatePermissionDTO;
import co.handk.common.model.vo.PermissionVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/permission")
public interface PermissionApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreatePermissionDTO dto);
    @GetMapping("/{id}")
    PermissionVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdatePermissionDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<PermissionVO> page(@Valid PermissionQueryDTO query);
}
