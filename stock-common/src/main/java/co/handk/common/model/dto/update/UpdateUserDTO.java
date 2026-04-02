package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;
    private String username;

    private Long deptId;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private StatusEnum status;
}
