package co.handk.backend.controller;

import co.handk.backend.service.OperateLogService;
import co.handk.common.model.PageResult;
import co.handk.common.model.vo.OperateLogVO;
import co.handk.common.model.dto.query.OperateLogQueryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/operateLog")
@RequiredArgsConstructor
public class OperateLogController {
    private final OperateLogService operateLogService;

    @GetMapping("/{id}")
    public OperateLogVO get(@PathVariable("id") Long id) {
        return operateLogService.getVOById(id);
    }

    @GetMapping("/page")
    public PageResult<OperateLogVO> page(@Valid OperateLogQueryDTO query) {
        return operateLogService.page(query);
    }
}

