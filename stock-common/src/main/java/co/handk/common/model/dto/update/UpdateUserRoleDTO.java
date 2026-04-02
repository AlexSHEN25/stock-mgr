package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private Long userId;
    private Long roleId;
}
