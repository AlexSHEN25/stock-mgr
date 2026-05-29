package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateUserRoleDTO {

    @NotNull(message = "ユーザーIDは必須項目です")
    private Long userId;
    @NotNull(message = "ロールIDは必須項目です")
    private Long roleId;
}
