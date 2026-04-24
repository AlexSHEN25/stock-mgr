package co.handk.backend.builder;

import co.handk.backend.annotation.QueryField;
import co.handk.common.enums.QueryType;
import co.handk.common.enums.DeleteEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public class QueryWrapperBuilder {

    public static <T, Q> QueryWrapper<T> build(Q dto) {

        QueryWrapper<T> wrapper = new QueryWrapper<>();

        // 默认逻辑删除
        wrapper.eq("deleted", DeleteEnum.UNDELETED.getCode());

        if (dto == null) {
            return wrapper;
        }

        Field[] fields = dto.getClass().getDeclaredFields();

        for (Field field : fields) {

            QueryField qf = field.getAnnotation(QueryField.class);
            if (qf == null) continue;

            field.setAccessible(true);

            try {
                Object value = field.get(dto);

                // 忽略 null
                if (value == null && qf.ignoreNull()) {
                    continue;
                }

                String column = qf.column().isEmpty()
                        ? camelToUnderline(field.getName())
                        : qf.column();

                applyCondition(wrapper, column, value, qf.type());

            } catch (Exception e) {
                throw new RuntimeException("构建查询条件失败", e);
            }
        }

        return wrapper;
    }

    private static <T> void applyCondition(QueryWrapper<T> wrapper,
                                           String column,
                                           Object value,
                                           QueryType type) {

        switch (type) {
            case EQ -> wrapper.eq(column, value);
            case LIKE -> wrapper.like(column, value);
            case GT -> wrapper.gt(column, value);
            case GE -> wrapper.ge(column, value);
            case LT -> wrapper.lt(column, value);
            case LE -> wrapper.le(column, value);
            case IN -> wrapper.in(column, (Collection<?>) value);
            case BETWEEN -> {
                List<?> list = (List<?>) value;
                if (list.size() == 2) {
                    wrapper.between(column, list.get(0), list.get(1));
                }
            }
        }
    }

    private static String camelToUnderline(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}