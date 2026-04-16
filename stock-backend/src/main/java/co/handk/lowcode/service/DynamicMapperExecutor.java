package co.handk.lowcode.service;

import co.handk.common.model.PageResult;
import co.handk.schema.registry.MapperRegistry;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 动态Mapper执行器（统一分页 + JOIN + CRUD）
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings({"rawtypes", "unchecked"})
public class DynamicMapperExecutor {

    private final MapperRegistry mapperRegistry;
    private final JdbcTemplate jdbcTemplate;

    /**
     * =========================
     * 1. 单表分页
     * =========================
     */
    public PageResult<?> page(
            Class<?> clazz,
            long pageNo,
            long pageSize,
            Wrapper<?> wrapper
    ) {

        BaseMapper mapper = mapperRegistry.get(clazz);

        Page<Object> page = new Page<>(pageNo, pageSize);

        IPage<Object> result = mapper.selectPage(page, (Wrapper) wrapper);

        return PageResult.build(
                result.getTotal(),
                pageNo,
                pageSize,
                result.getRecords()
        );
    }

    /**
     * =========================
     * 2. JOIN分页（原生SQL）
     * =========================
     */
    public PageResult<Map<String, Object>> pageJoin(
            String sql,
            long page,
            long size
    ) {

        long offset = (page - 1) * size;

        String pageSql = sql + " LIMIT " + offset + "," + size;

        List<Map<String, Object>> list = jdbcTemplate.queryForList(pageSql);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" + sql + ") t",
                Long.class
        );

        return PageResult.build(
                total,
                page,
                size,
                list
        );
    }

    /**
     * =========================
     * 3. 详情
     * =========================
     */
    public Object getById(Class<?> clazz, Long id) {
        BaseMapper mapper = mapperRegistry.get(clazz);
        return mapper.selectById(id);
    }

    /**
     * =========================
     * 4. 新增
     * =========================
     */
    public void insert(Object entity) {
        BaseMapper mapper = mapperRegistry.get(entity.getClass());
        mapper.insert(entity);
    }

    /**
     * =========================
     * 5. 更新
     * =========================
     */
    public void updateById(Object entity) {
        BaseMapper mapper = mapperRegistry.get(entity.getClass());
        mapper.updateById(entity);
    }

    /**
     * =========================
     * 6. 删除
     * =========================
     */
    public void deleteById(Class<?> clazz, Long id) {
        BaseMapper mapper = mapperRegistry.get(clazz);
        mapper.deleteById(id);
    }
}