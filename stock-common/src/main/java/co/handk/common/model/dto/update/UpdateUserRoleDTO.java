package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UpdateUserRoleDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotNull(message = "ユーザーIDは必須項目です")
    private Long userId;
    @NotNull(message = "ロールIDは必須項目です")
    private Long roleId;
}
