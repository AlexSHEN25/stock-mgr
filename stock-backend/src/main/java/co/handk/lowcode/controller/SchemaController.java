package co.handk.lowcode.controller;

import co.handk.schema.builder.SchemaBuilder;
import co.handk.schema.model.SchemaVO;
import co.handk.schema.registry.SchemaRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Schema接口（前端驱动核心）
 */
@RestController
@RequestMapping("/api/schema")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaRegistry registry;

    /**
     * 获取单个Schema
     */
    @GetMapping("/{resource}")
    public SchemaVO get(@PathVariable("resource") String resource) {
        Class<?> clazz = registry.get(resource);
        return SchemaBuilder.build(clazz);
    }

    /**
     * 获取全部Schema（用于菜单）
     */
    @GetMapping
    public List<SchemaVO> list() {
        return registry.getAll().values().stream()
                .map(SchemaBuilder::build)
                .collect(Collectors.toList());
    }
}
