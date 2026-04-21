package co.handk.schema.builder;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaAction;
import co.handk.schema.annotation.SchemaActions;
import co.handk.schema.annotation.SchemaField;
import co.handk.schema.model.SchemaVO;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

/**
 * Schema 构建器（反射解析注解 → 前端Schema）
 */
public class SchemaBuilder {

    public static SchemaVO build(Class<?> clazz) {

        Schema schema = clazz.getAnnotation(Schema.class);
        if (schema == null) {
            return null;
        }

        SchemaVO vo = new SchemaVO();

        // ===== 基础信息 =====
        vo.setResource(schema.resource());
        vo.setName(schema.name());
        String groupPath = schema.group();
        vo.setGroupPath(groupPath);
        if (groupPath != null && groupPath.contains("/")) {
            String[] parts = groupPath.split("/", 2);
            vo.setGroup(parts[0]);
            vo.setSubGroup(parts[1]);
        } else {
            vo.setGroup(groupPath);
            vo.setSubGroup(null);
        }
        String topMenuId = toMenuId("top", vo.getGroup());
        String subMenuId = vo.getSubGroup() == null ? null : toMenuId("sub", vo.getGroup() + "/" + vo.getSubGroup());
        String defaultMenuId = "res_" + schema.resource();
        String defaultParentId = subMenuId == null ? topMenuId : subMenuId;
        vo.setTopMenuId(topMenuId);
        vo.setSubMenuId(subMenuId);
        vo.setMenuId(schema.menuId().isEmpty() ? defaultMenuId : schema.menuId());
        vo.setParentId(schema.parentId().isEmpty() ? defaultParentId : schema.parentId());

        // ===== 字段解析 =====
        List<SchemaVO.FieldVO> fields = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {

            SchemaField sf = field.getAnnotation(SchemaField.class);
            if (sf == null) continue;

            //  完全隐藏字段（安全优化）
            if (!sf.table() && !sf.search() && !sf.detail()) {
                continue;
            }

            SchemaVO.FieldVO f = new SchemaVO.FieldVO();

            f.setName(field.getName());
            f.setTitle(sf.title());
            f.setType(sf.type().name());
            f.setRequired(sf.required());
            f.setTable(sf.table());
            f.setSearch(sf.search());

            // ===== 字典 =====
            if (!sf.dict().code().isEmpty()) {
                f.setDict(sf.dict().code());
            }

            // ===== 关联（重点升级） =====
            if (!sf.ref().resource().isEmpty()) {
                f.setRef(sf.ref().resource());

                // 核心：前端 select 需要的字段
                f.setRefLabelField(sf.ref().labelField());
                f.setRefValueField(sf.ref().valueField());

                // 新增
                f.setRefDisplayField(
                        sf.ref().displayField().isEmpty()
                                ? field.getName() + "Name" // 默认兼容
                                : sf.ref().displayField()
                );

            }

            fields.add(f);
        }

        // ===== 字段排序（支持 order） =====
        fields.sort(Comparator.comparingInt(f -> {
            try {
                Field field = clazz.getDeclaredField(f.getName());
                SchemaField sf = field.getAnnotation(SchemaField.class);
                return sf.order();
            } catch (Exception e) {
                return 0;
            }
        }));

        vo.setFields(fields);

        // ===== 操作解析 =====
        List<SchemaVO.ActionVO> actions = new ArrayList<>();

        // 多个 Action
        SchemaActions schemaActions = clazz.getAnnotation(SchemaActions.class);
        if (schemaActions != null) {
            for (SchemaAction action : schemaActions.value()) {
                actions.add(buildAction(action));
            }
        }

        // 单个 Action
        SchemaAction single = clazz.getAnnotation(SchemaAction.class);
        if (single != null) {
            actions.add(buildAction(single));
        }

        vo.setActions(actions);

        return vo;
    }

    /**
     * 构建 Action
     */
    private static SchemaVO.ActionVO buildAction(SchemaAction action) {
        SchemaVO.ActionVO vo = new SchemaVO.ActionVO();
        vo.setCode(action.code());
        vo.setName(action.name());
        vo.setApi(action.api());
        vo.setMethod(action.method().name());
        return vo;
    }

    private static String toMenuId(String prefix, String value) {
        if (value == null || value.isEmpty()) {
            return prefix + "_default";
        }
        String encoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
        return prefix + "_" + encoded;
    }
}
