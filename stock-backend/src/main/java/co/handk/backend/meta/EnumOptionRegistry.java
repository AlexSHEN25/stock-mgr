package co.handk.backend.meta;

import co.handk.common.enums.OptionEnum;
import co.handk.common.enums.PermissionTypeEnum;
import co.handk.common.enums.StatusEnum;
import co.handk.common.model.vo.EnumOptionVO;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class EnumOptionRegistry {

    private EnumOptionRegistry() {
    }

    private static final Map<String, Supplier<List<EnumOptionVO>>> ENUM_SUPPLIERS = Map.of(
            "permissionType", () -> toOptions(PermissionTypeEnum.values()),
            "status", () -> toOptions(StatusEnum.values())
    );

    public static List<EnumOptionVO> getOptions(String enumKey) {
        Supplier<List<EnumOptionVO>> supplier = ENUM_SUPPLIERS.get(enumKey);
        if (supplier == null) {
            return List.of();
        }
        return supplier.get();
    }

    private static List<EnumOptionVO> toOptions(OptionEnum[] values) {
        return java.util.Arrays.stream(values)
                .map(v -> new EnumOptionVO(v.getCode(), v.getLabel()))
                .toList();
    }
}

