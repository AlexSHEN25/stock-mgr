package co.handk.api;

import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateDeptDTO;
import co.handk.common.model.dto.query.DeptQueryDTO;
import co.handk.common.model.dto.update.UpdateDeptDTO;
import co.handk.common.model.vo.DeptVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/dept")
public interface DeptApi {
    @PostMapping
    Boolean create(@RequestBody @NotNull @Valid CreateDeptDTO dto);
    @GetMapping("/{id}")
    DeptVO get(@PathVariable @NotNull Long id);
    @PutMapping
    Boolean update(@RequestBody @NotNull @Valid UpdateDeptDTO dto);
    @DeleteMapping("/{id}")
    Boolean delete(@PathVariable @NotNull Long id);
    @GetMapping("/page")
    PageResult<DeptVO> page(@Valid DeptQueryDTO query);
}
