package co.handk.backend.controller;

import co.handk.backend.entity.OperateLog;
import co.handk.backend.service.OperateLogService;
import co.handk.common.model.PageQuery;
import co.handk.common.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operateLog")
public class OperateLogController {

    @Autowired
    private OperateLogService operateLogService;

    @PostMapping
    public Boolean create(@RequestBody OperateLog entity) {
        return operateLogService.create(entity);
    }

    @GetMapping("/{id}")
    public OperateLog get(@PathVariable Long id) {
        return operateLogService.get(id);
    }

    @PutMapping
    public Boolean update(@RequestBody OperateLog entity) {
        return operateLogService.update(entity);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Long id) {
        return operateLogService.delete(id);
    }

    @GetMapping("/list")
    public List<OperateLog> list() {
        return operateLogService.listAll();
    }

    @GetMapping("/page")
    public PageResult<OperateLog> page(PageQuery query) {
        return operateLogService.pageQuery(query);
    }
}
