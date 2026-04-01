package co.handk.backend.controller;

import co.handk.backend.entity.OperateLog;
import co.handk.common.model.vo.OperateLogVO;
import co.handk.common.model.dto.OperateLogDTO;
import co.handk.backend.service.OperateLogService;
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
@RequestMapping("/operateLog")
public class OperateLogController {

    @Autowired
    private OperateLogService operateLogService;

    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid OperateLogDTO dto) {
        return operateLogService.create(dto);
    }

    @GetMapping("/{id}")
    public OperateLogVO get(@PathVariable @NotNull Long id) {
        return operateLogService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid OperateLogDTO dto) {
        return operateLogService.update(dto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return operateLogService.delete(id);
    }

    @GetMapping("/list")
    public List<OperateLogVO> list() {
        return operateLogService.listAll();
    }

    @GetMapping("/page")
    public PageResult<OperateLogVO> page(@Valid PageQuery query) {
        return operateLogService.pageQuery(query);
    }
}
