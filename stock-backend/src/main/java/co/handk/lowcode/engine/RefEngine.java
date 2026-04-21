package co.handk.lowcode.engine;

import co.handk.schema.model.SchemaVO;
import co.handk.schema.registry.MapperRegistry;
import co.handk.schema.registry.SchemaRegistry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 低代码关联字段填充引擎。
 */
@Component
@RequiredArgsConstructor
public class RefEngine {

    private final SchemaRegistry registry;
    private final MapperRegistry mapperRegistry;

    public <T> List<T> fillRefs(List<T> records, SchemaVO schema) {
        if (records == null || records.isEmpty() || schema == null || schema.getFields() == null) {
            return records;
        }

        for (SchemaVO.FieldVO field : schema.getFields()) {
            if (field.getRef() == null || field.getRef().isBlank()) {
                continue;
            }

            Set<Object> ids = records.stream()
                    .map(row -> getFieldValue(row, field.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (ids.isEmpty()) {
                continue;
            }

            Map<Object, Object> refMap = loadRefData(
                    field.getRef(),
                    ids,
                    field.getRefLabelField(),
                    field.getRefValueField()
            );

            String displayField = field.getRefDisplayField();
            for (T row : records) {
                Object id = getFieldValue(row, field.getName());
                if (id == null) {
                    continue;
                }
                Object label = refMap.get(String.valueOf(id));
                if (label == null) {
                    continue;
                }
                setFieldValue(row, displayField, label);
            }
        }

        return records;
    }

    private Map<Object, Object> loadRefData(
            String resource,
            Set<Object> ids,
            String labelField,
            String valueField
    ) {
        Class<?> refEntityClass = registry.get(resource);
        @SuppressWarnings("unchecked")
        BaseMapper<Object> mapper = (BaseMapper<Object>) mapperRegistry.get((Class<Object>) refEntityClass);
        List<Serializable> serializableIds = ids.stream()
                .filter(Serializable.class::isInstance)
                .map(Serializable.class::cast)
                .toList();
        List<Object> rows = mapper.selectBatchIds(serializableIds);

        Map<Object, Object> result = new HashMap<>();
        for (Object row : rows) {
            Object key = getFieldValue(row, valueField);
            Object label = getFieldValue(row, labelField);
            if (key != null) {
                result.put(String.valueOf(key), label);
            }
        }
        return result;
    }

    private Object getFieldValue(Object obj, String fieldName) {
        Field field = findField(obj.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private void setFieldValue(Object obj, String fieldName, Object value) {
        Field field = findField(obj.getClass(), fieldName);
        if (field == null) {
            return;
        }
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
