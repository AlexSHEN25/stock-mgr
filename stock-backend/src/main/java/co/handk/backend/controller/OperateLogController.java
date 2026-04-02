package co.handk.backend.controller;

import co.handk.api.OperateLogApi;
import co.handk.common.model.vo.OperateLogVO;
import co.handk.common.model.dto.create.CreateOperateLogDTO;
import co.handk.common.model.dto.update.UpdateOperateLogDTO;
import co.handk.backend.service.OperateLogService;
import co.handk.common.model.dto.query.OperateLogQueryDTO;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@Validated
@RequestMapping("/operateLog")
public class OperateLogController implements OperateLogApi {
    @Autowired
    private OperateLogService operateLogService;
    @PostMapping
    public Boolean create(@RequestBody @NotNull @Valid CreateOperateLogDTO dto) {
        return operateLogService.create(dto);
    }
    @GetMapping("/{id}")
    public OperateLogVO get(@PathVariable @NotNull Long id) {
        return operateLogService.get(id);
    }
    @PutMapping
    public Boolean update(@RequestBody @NotNull @Valid UpdateOperateLogDTO dto) {
        return operateLogService.update(dto);
    }
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable @NotNull Long id) {
        return operateLogService.delete(id);
    }
    @GetMapping("/page")
    public PageResult<OperateLogVO> page(@Valid OperateLogQueryDTO query) {
        return operateLogService.pageQuery(query);
    }
}
