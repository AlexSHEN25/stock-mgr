package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateRolePermissionDTO {

    @NotNull(message = "ロールIDは必須項目です")
    private Long roleId;
    @NotNull(message = "権限IDは必須項目です")
    private Long permissionId;
}
