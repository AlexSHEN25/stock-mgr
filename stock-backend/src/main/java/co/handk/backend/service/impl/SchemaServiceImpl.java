package co.handk.backend.service.impl;

import co.handk.backend.model.schema.MenuItemVO;
import co.handk.backend.model.schema.SchemaColumnVO;
import co.handk.backend.model.schema.SchemaVO;
import co.handk.backend.service.SchemaService;
import co.handk.common.annotation.SchemaField;
import co.handk.common.enums.SchemaControlType;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SchemaServiceImpl implements SchemaService {

    private static final String VO_PACKAGE = "co.handk.common.model.vo.";
    private static final Map<String, String> JP_LABELS = new HashMap<>();

    static {
        JP_LABELS.put("id", "ID");
        JP_LABELS.put("createTime", "\u4f5c\u6210\u65e5\u6642");
        JP_LABELS.put("updateTime", "\u66f4\u65b0\u65e5\u6642");
        JP_LABELS.put("status", "\u72b6\u614b\u30b3\u30fc\u30c9");
        JP_LABELS.put("statusDesc", "\u72b6\u614b");
        JP_LABELS.put("username", "\u30e6\u30fc\u30b6\u30fc\u540d");
        JP_LABELS.put("deptId", "\u90e8\u7f72ID");
        JP_LABELS.put("deptName", "\u90e8\u7f72\u540d");
        JP_LABELS.put("email", "\u30e1\u30fc\u30eb");
        JP_LABELS.put("phone", "\u96fb\u8a71\u756a\u53f7");
        JP_LABELS.put("goodsName", "\u5546\u54c1\u540d");
        JP_LABELS.put("skuCode", "SKU\u30b3\u30fc\u30c9");
        JP_LABELS.put("currency", "\u901a\u8ca8");
        JP_LABELS.put("price", "\u4fa1\u683c");
    }

    @Override
    public SchemaVO getSchema(String name) {
        Class<?> voClass = resolveVoClass(name);
        List<SchemaColumnVO> columns = buildColumns(voClass);
        return new SchemaVO(voClass.getSimpleName(), columns);
    }

    @Override
    public List<MenuItemVO> getMenuSchema() {
        return Collections.singletonList(
                new MenuItemVO(100L, 0L, "\u30b7\u30b9\u30c6\u30e0\u7ba1\u7406", 1, "/system", Collections.emptyList())
        );
    }

    private Class<?> resolveVoClass(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("schema name is required");
        }
        String normalized = name.trim();
        String simpleName = normalized.endsWith("VO") ? normalized : toPascal(normalized) + "VO";
        try {
            return Class.forName(VO_PACKAGE + simpleName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("schema VO not found: " + simpleName);
        }
    }

    private List<SchemaColumnVO> buildColumns(Class<?> voClass) {
        List<SchemaColumnVO> columns = new ArrayList<>();
        for (Field field : getAllFields(voClass)) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            String fieldName = field.getName();
            SchemaField schemaField = field.getAnnotation(SchemaField.class);
            String label = JP_LABELS.getOrDefault(fieldName, schemaField != null ? schemaField.label() : fieldName);
            int order = schemaField != null ? schemaField.order() : 1000;
            if ("createTime".equals(fieldName)) {
                order = 9998;
            } else if ("updateTime".equals(fieldName)) {
                order = 9999;
            }
            boolean visible = schemaField == null || schemaField.visible();
            boolean editable = schemaField == null || schemaField.editable();
            String controlType = resolveControlType(schemaField, fieldName, field.getType());
            String dataType = inferDataType(field.getType());
            columns.add(new SchemaColumnVO(fieldName, label, order, visible, editable, controlType, dataType));
        }
        columns.sort(Comparator.comparing(SchemaColumnVO::getOrder).thenComparing(SchemaColumnVO::getField));
        return columns;
    }

    private String resolveControlType(SchemaField schemaField, String fieldName, Class<?> type) {
        if (schemaField != null && schemaField.controlType() != SchemaControlType.AUTO) {
            return schemaField.controlType().name().toLowerCase(Locale.ROOT);
        }
        return inferControlType(fieldName, type);
    }

    private String inferControlType(String fieldName, Class<?> type) {
        String lower = fieldName.toLowerCase(Locale.ROOT);
        if (lower.endsWith("status") || lower.endsWith("type") || lower.endsWith("state")) {
            return "select";
        }
        if (lower.contains("remark") || lower.contains("description") || lower.contains("content")) {
            return "textarea";
        }
        if (lower.contains("time") || type == LocalDateTime.class) {
            return "datetime";
        }
        if (type == LocalDate.class) {
            return "date";
        }
        if (type == Boolean.class || type == boolean.class || lower.startsWith("is")) {
            return "switch";
        }
        if (Number.class.isAssignableFrom(type)
                || type == int.class || type == long.class || type == double.class || type == float.class) {
            return "number";
        }
        return "input";
    }

    private String inferDataType(Class<?> type) {
        if (type == LocalDateTime.class) {
            return "datetime";
        }
        if (type == LocalDate.class) {
            return "date";
        }
        if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        }
        if (Number.class.isAssignableFrom(type)
                || type == int.class || type == long.class || type == double.class || type == float.class) {
            return "number";
        }
        return "string";
    }

    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private String toPascal(String raw) {
        String normalized = raw.replace("-", "_");
        String[] parts = normalized.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }
}
