package co.handk.backend.controller;

import co.handk.backend.service.DeptService;
import co.handk.common.model.PageResult;
import co.handk.common.model.dto.create.CreateDeptDTO;
import co.handk.common.model.dto.query.DeptQueryDTO;
import co.handk.common.model.dto.update.UpdateDeptDTO;
import co.handk.common.model.vo.DeptVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/dept")
public class DeptController {
    @Autowired
    private DeptService deptService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateDeptDTO dto) {
        return deptService.create(dto);
    }
    @GetMapping("/{id}")
    public DeptVO get(@PathVariable @NotNull Long id) {
        return deptService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateDeptDTO dto) {
        return deptService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return deptService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<DeptVO> page(@Valid DeptQueryDTO query) {
        return deptService.pageQuery(query);
    }
}
