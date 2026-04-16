package co.handk.schema.registry;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper注册中心（企业级稳定版）
 */
@Component
@RequiredArgsConstructor
public class MapperRegistry {

    private final ApplicationContext applicationContext;

    private final Map<Class<?>, BaseMapper<?>> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {

        Map<String, BaseMapper> beans =
                applicationContext.getBeansOfType(BaseMapper.class);

        for (BaseMapper<?> mapper : beans.values()) {

            Class<?> entityClass = resolveEntityType(mapper);

            if (entityClass == null) {
                System.err.println("[LowCode] 未解析Mapper泛型: " + mapper.getClass());
                continue;
            }

            if (cache.containsKey(entityClass)) {
                throw new RuntimeException("Mapper重复绑定: " + entityClass.getName());
            }

            cache.put(entityClass, mapper);

            System.out.println("[LowCode] Mapper注册: "
                    + entityClass.getSimpleName()
                    + " -> "
                    + mapper.getClass().getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> BaseMapper<T> get(Class<T> entityClass) {

        BaseMapper<?> mapper = cache.get(entityClass);

        if (mapper == null) {
            throw new RuntimeException("未找到Mapper: " + entityClass.getName());
        }

        return (BaseMapper<T>) mapper;
    }

    private Class<?> resolveEntityType(Object mapper) {
        Class<?> targetClass = AopUtils.getTargetClass(mapper);
        return findEntityType(targetClass);
    }

    private Class<?> findEntityType(Class<?> clazz) {

        if (clazz == null || clazz == Object.class) {
            return null;
        }

        for (Type type : clazz.getGenericInterfaces()) {
            Class<?> entity = extractEntity(type);
            if (entity != null) {
                return entity;
            }
        }

        return findEntityType(clazz.getSuperclass());
    }

    private Class<?> extractEntity(Type type) {

        if (!(type instanceof ParameterizedType pt)) {
            return null;
        }

        if (!(pt.getRawType() instanceof Class<?> rawClass)) {
            return null;
        }

        if (BaseMapper.class.isAssignableFrom(rawClass)) {

            Type entityType = pt.getActualTypeArguments()[0];

            if (entityType instanceof Class<?>) {
                return (Class<?>) entityType;
            }
        }

        return null;
    }
}