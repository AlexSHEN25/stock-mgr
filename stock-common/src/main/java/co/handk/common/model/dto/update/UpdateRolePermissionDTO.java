package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRolePermissionDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotNull(message = "ロールIDは必須項目です")
    private Long roleId;
    @NotNull(message = "権限IDは必須項目です")
    private Long permissionId;
}
