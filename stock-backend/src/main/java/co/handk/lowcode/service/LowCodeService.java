package co.handk.lowcode.service;

import cn.hutool.core.bean.BeanUtil;
import co.handk.common.model.PageResult;
import co.handk.lowcode.engine.PageParamParser;
import co.handk.schema.builder.QueryBuilder;
import co.handk.schema.registry.SchemaRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 低代码核心引擎（最终稳定版）
 */
@Service
@RequiredArgsConstructor
public class LowCodeService {

    private final SchemaRegistry schemaRegistry;
    private final DynamicMapperExecutor mapperExecutor;

    /**
     * 分页查询
     */
    public PageResult<?> page(String resource, Map<String, Object> params) {

        Class<?> entityClass = schemaRegistry.get(resource);

        long pageNo = PageParamParser.getPage(params);
        long pageSize = PageParamParser.getSize(params);

        var wrapper = QueryBuilder.build(entityClass, params);

        return mapperExecutor.page(entityClass, pageNo, pageSize, wrapper);
    }

    /**
     * 详情
     */
    public Object detail(String resource, Long id) {
        Class<?> entityClass = schemaRegistry.get(resource);
        return mapperExecutor.getById(entityClass, id);
    }

    /**
     * 新增
     */
    public Object create(String resource, Map<String, Object> body) {
        Class<?> entityClass = schemaRegistry.get(resource);
        Object entity = BeanUtil.mapToBean(body, entityClass, true);
        mapperExecutor.insert(entity);
        return entity;
    }

    /**
     * 更新
     */
    public Object update(String resource, Long id, Map<String, Object> body) {
        Class<?> entityClass = schemaRegistry.get(resource);
        Object entity = BeanUtil.mapToBean(body, entityClass, true);

        // 设置ID（关键）
        BeanUtil.setFieldValue(entity, "id", id);

        mapperExecutor.updateById(entity);
        return entity;
    }

    /**
     * 删除
     */
    public Object delete(String resource, Long id) {
        Class<?> entityClass = schemaRegistry.get(resource);
        mapperExecutor.deleteById(entityClass, id);
        return true;
    }
}