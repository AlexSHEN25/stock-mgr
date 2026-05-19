package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRoleDTO {

    @NotNull(message = "ユーザーIDは必須項目です")
    private Long userId;
    @NotNull(message = "ロールIDは必須項目です")
    private Long roleId;
}
