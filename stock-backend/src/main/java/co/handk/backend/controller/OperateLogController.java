package co.handk.backend.controller;

import jakarta.validation.constraints.NotNull;

import co.handk.common.constant.NumberConstant;

import co.handk.backend.service.OperateLogService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.*;
import co.handk.common.model.dto.create.CreateOperateLogDTO;
import co.handk.common.model.dto.query.OperateLogQueryDTO;
import co.handk.common.model.dto.update.UpdateOperateLogDTO;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/operateLog")
public class OperateLogController {
    @Autowired
    private OperateLogService operateLogService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateOperateLogDTO dto) {
        return operateLogService.saveByDto(dto);
    }

    @GetMapping("/{id}")
    public OperateLogVO get(@PathVariable("id") @NotNull Long id) {
        return operateLogService.getVOById(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateOperateLogDTO dto) {
        return operateLogService.updateByDto(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable("id") @NotNull Long id) {
        return operateLogService.deleteByIdLogic(id) > NumberConstant.ZERO;
    }

    @DeleteMapping("/batch")
    public Boolean deleteBatch(@RequestBody @NotNull List<Long> ids) {
        return operateLogService.deleteBatchLogic(ids) > NumberConstant.ZERO;
    }

    @GetMapping("/page")
    public PageResult<OperateLogVO> page(@Valid OperateLogQueryDTO query) {
        return operateLogService.page(query);
    }
}


