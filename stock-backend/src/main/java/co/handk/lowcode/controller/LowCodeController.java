package co.handk.lowcode.controller;

import co.handk.lowcode.service.LowCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 低代码统一入口（所有CRUD走这里）
 */
@RestController
@RequestMapping("/api/lowcode")
@RequiredArgsConstructor
public class LowCodeController {

    private final LowCodeService lowCodeService;

    /**
     * 分页查询
     * GET /api/lowcode/user?page=1&size=10
     */
    @GetMapping("/{resource}")
    public Object page(
            @PathVariable("resource") String resource,
            @RequestParam Map<String, Object> params
    ) {
        return lowCodeService.page(resource, params);
    }

    /**
     * 详情
     */
    @GetMapping("/{resource}/{id}")
    public Object detail(@PathVariable("resource") String resource, @PathVariable("id") Long id) {
        return lowCodeService.detail(resource, id);
    }

    /**
     * 新增
     */
    @PostMapping("/{resource}")
    public Object create(@PathVariable("resource") String resource, @RequestBody Map<String, Object> body) {
        return lowCodeService.create(resource, body);
    }

    /**
     * 更新
     */
    @PutMapping("/{resource}/{id}")
    public Object update(
            @PathVariable("resource") String resource,
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> body
    ) {
        return lowCodeService.update(resource, id, body);
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{resource}/{id}")
    public Object delete(@PathVariable("resource") String resource, @PathVariable("id") Long id) {
        return lowCodeService.delete(resource, id);
    }
}
