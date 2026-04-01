package co.handk.backend.controller;

import co.handk.backend.entity.Dept;
import co.handk.common.model.vo.DeptVO;
import co.handk.common.model.dto.DeptDTO;
import co.handk.backend.service.DeptService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/dept")
public class DeptController {

    @Autowired
    private DeptService deptService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid DeptDTO dto) {
        return deptService.create(dto);
    }

    @GetMapping("/{id}")
    public DeptVO get(@PathVariable @NotNull Long id) {
        return deptService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid DeptDTO dto) {
        return deptService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return deptService.delete(id);
    }

    @GetMapping("/list")
    public List<DeptVO> list() {
        return deptService.listAll();
    }

    @GetMapping("/page")
    public PageResult<DeptVO> page(@Valid PageQuery query) {
        return deptService.pageQuery(query);
    }
}
