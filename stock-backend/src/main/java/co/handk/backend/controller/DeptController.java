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
        return deptService.saveByDto(dto);
    }
    @GetMapping("/{id}")
    public DeptVO get(@PathVariable("id") @NotNull Long id) {
        return deptService.getVOById(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateDeptDTO dto) {
        return deptService.updateByDto(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return deptService.deleteByIdLogic(id) > 0;
    }
    @GetMapping("/page")
    public PageResult<DeptVO> page(@Valid DeptQueryDTO query) {
        return deptService.page(query);
    }
}

