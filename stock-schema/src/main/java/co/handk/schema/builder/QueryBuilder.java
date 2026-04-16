package co.handk.schema.builder;

import co.handk.schema.annotation.SchemaField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;


/**
 * 动态查询构建器（核心引擎）
 */
public class QueryBuilder {

    public static QueryWrapper<Object> build(Class<?> clazz, Map<String, Object> params) {

        QueryWrapper<Object> wrapper = new QueryWrapper<>();

        for (Field field : clazz.getDeclaredFields()) {

            SchemaField sf = field.getAnnotation(SchemaField.class);
            if (sf == null || !sf.search()) continue;

            String fieldName = field.getName();

            if (!params.containsKey(fieldName)) continue;

            Object value = params.get(fieldName);

            if (value == null || value.toString().isEmpty()) continue;

            // 按类型构建查询
            switch (sf.queryType()) {

                case EQ:
                    wrapper.eq(fieldName, value);
                    break;

                case LIKE:
                    wrapper.like(fieldName, value);
                    break;

                case GT:
                    wrapper.gt(fieldName, value);
                    break;

                case LT:
                    wrapper.lt(fieldName, value);
                    break;

                case BETWEEN:
                    if (value instanceof List list && list.size() == 2) {
                        wrapper.between(fieldName, list.get(0), list.get(1));
                    }
                    break;
            }
        }

        return wrapper;
    }
}