package co.handk.backend.util;

import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;

import java.lang.reflect.Method;

/**
 * DTO 枚举字段到实体数值字段的映射工具。
 */
public final class EnumFieldMapper {

    private EnumFieldMapper() {
    }

    public static void mapStatusAndDeleted(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        mapStatus(source, target);
        mapDeleted(source, target);
    }

    private static void mapStatus(Object source, Object target) {
        Method getter = findMethod(source.getClass(), "getStatus");
        if (getter == null) {
            return;
        }
        Object value = invoke(getter, source);
        if (!(value instanceof StatusEnum)) {
            return;
        }
        StatusEnum status = (StatusEnum) value;
        Method setter = findIntegerSetter(target.getClass(), "setStatus");
        if (setter == null) {
            return;
        }
        invoke(setter, target, status == null ? null : status.getCode());
    }

    private static void mapDeleted(Object source, Object target) {
        Method getter = findMethod(source.getClass(), "getDeleted");
        if (getter == null) {
            return;
        }
        Object value = invoke(getter, source);
        if (!(value instanceof DeleteEnum)) {
            return;
        }
        DeleteEnum deleted = (DeleteEnum) value;
        Method setter = findIntegerSetter(target.getClass(), "setDeleted");
        if (setter == null) {
            return;
        }
        invoke(setter, target, deleted == null ? null : deleted.getCode());
    }

    private static Method findMethod(Class<?> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Method findIntegerSetter(Class<?> type, String name) {
        try {
            return type.getMethod(name, Integer.class);
        } catch (NoSuchMethodException e) {
            try {
                return type.getMethod(name, int.class);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new IllegalStateException("Enum mapping failed", e);
        }
    }
}
