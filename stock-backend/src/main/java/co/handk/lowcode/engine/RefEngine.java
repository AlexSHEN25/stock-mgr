package co.handk.lowcode.engine;

import co.handk.schema.model.SchemaVO;
import co.handk.schema.registry.SchemaRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自动关联填充引擎（核心）
 */
@Component
@RequiredArgsConstructor
public class RefEngine {

    private final SchemaRegistry registry;

    /**
     * 批量填充关联字段
     */
    public List<Map<String, Object>> fillRefs(
            List<Map<String, Object>> list,
            SchemaVO schema
    ) {

        if (list == null || list.isEmpty()) return list;

        for (SchemaVO.FieldVO field : schema.getFields()) {

            if (field.getRef() == null) continue;

            String refResource = field.getRef();

            //  收集所有 deptId
            Set<Object> ids = list.stream()
                    .map(row -> row.get(field.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (ids.isEmpty()) continue;

            //  查询关联数据
            Map<Object, Object> refMap = loadRefData(
                    refResource,
                    ids,
                    field.getRefLabelField(),
                    field.getRefValueField()
            );

            //  回填
            for (Map<String, Object> row : list) {

                Object id = row.get(field.getName());
                if (id == null) continue;

                Object label = refMap.get(id);

                String displayField = field.getRefDisplayField();
                row.put(displayField, label);
            }
        }

        return list;
    }

    /**
     * 查询关联数据（核心扩展点）
     */
    private Map<Object, Object> loadRefData(
            String resource,
            Set<Object> ids,
            String labelField,
            String valueField
    ) {

        Class<?> clazz = registry.get(resource);

        //  这里你可以接 MyBatis-Plus
        // 简化写法：用反射 or Service 查询

        // ===== 示例（伪代码）=====
        /*
        List<?> list = service.listByIds(ids);

        return list.stream().collect(Collectors.toMap(
            e -> getField(e, valueField),
            e -> getField(e, labelField)
        ));
        */

        return new HashMap<>();
    }
}