package co.handk.schema.builder;

import co.handk.schema.model.SchemaVO;
import co.handk.schema.registry.SchemaRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JOIN SQL 构建器（核心引擎）
 */
@Component
@RequiredArgsConstructor
public class JoinQueryBuilder {

    private final SchemaRegistry schemaRegistry;

    public String buildSelectSql(Class<?> mainClass, SchemaVO schema) {

        String mainTable = getTableName(mainClass);
        String mainAlias = "t0";

        StringBuilder select = new StringBuilder("SELECT ");
        StringBuilder from = new StringBuilder(" FROM " + mainTable + " " + mainAlias);

        //  主表字段
        schema.getFields().forEach(f -> {
            if (f.isTable()) {
                select.append(mainAlias).append(".")
                        .append(toColumn(f.getName()))
                        .append(", ");
            }
        });

        int joinIndex = 1;

        //  JOIN处理
        for (SchemaVO.FieldVO f : schema.getFields()) {

            if (f.getRef() == null) continue;

            Class<?> refClass = schemaRegistry.get(f.getRef());

            String refTable = getTableName(refClass);
            String refAlias = "t" + joinIndex++;

            //  LEFT JOIN
            from.append(" LEFT JOIN ")
                    .append(refTable).append(" ").append(refAlias)
                    .append(" ON ")
                    .append(mainAlias).append(".").append(toColumn(f.getName()))
                    .append(" = ")
                    .append(refAlias).append(".").append(f.getRefValueField());

            //  select label
            select.append(refAlias).append(".")
                    .append(f.getRefLabelField())
                    .append(" AS ")
                    .append(f.getName().replace("Id", "Name"))
                    .append(", ");
        }

        // 去掉最后逗号
        select.setLength(select.length() - 2);

        return select.toString() + from.toString();
    }

    private String getTableName(Class<?> clazz) {
        return "t_" + clazz.getSimpleName().toLowerCase();
    }

    private String toColumn(String field) {
        return field.replaceAll("([A-Z])", "_$1").toLowerCase();
    }
}