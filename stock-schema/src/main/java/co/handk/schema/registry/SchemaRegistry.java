package co.handk.schema.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Schema注册中心（线程安全）
 */
@Component
public class SchemaRegistry {

    /**
     * resource → entityClass
     */
    private final Map<String, Class<?>> registry = new ConcurrentHashMap<>();

    /**
     * 注册
     */
    public void register(String resource, Class<?> clazz) {

        if (registry.containsKey(resource)) {
            throw new RuntimeException("重复注册资源: " + resource);
        }

        registry.put(resource, clazz);
    }

    /**
     * 获取
     */
    public Class<?> get(String resource) {

        Class<?> clazz = registry.get(resource);

        if (clazz == null) {
            throw new RuntimeException("未找到资源: " + resource);
        }

        return clazz;
    }

    /**
     * 全量（给前端用）
     */
    public Map<String, Class<?>> getAll() {
        return registry;
    }
}