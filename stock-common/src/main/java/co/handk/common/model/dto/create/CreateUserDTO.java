package co.handk.common.model.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotNull(message = "部门不能为空")
    private Long deptId;
    @NotBlank(message = "密码不能为空")
    private String password;
    @Email(message = "邮箱格式不正确")
    private String email;
    private String phone;
    private String avatar;
}
