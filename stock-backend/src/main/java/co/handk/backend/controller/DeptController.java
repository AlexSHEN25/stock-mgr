package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.DeptService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateDeptDTO;
import co.handk.common.model.dto.query.DeptQueryDTO;
import co.handk.common.model.dto.update.UpdateDeptDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/dept")
@RequiredArgsConstructor
public class DeptController {
    private final DeptService deptService;

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
        return deptService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return deptService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<DeptVO> page(@Valid DeptQueryDTO query) {
        return deptService.page(query);
    }
}

