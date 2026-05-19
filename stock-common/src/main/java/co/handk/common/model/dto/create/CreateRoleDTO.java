package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleDTO {

    @NotBlank(message = "名称は必須項目です")
    private String name;
    @NotBlank(message = "ロールコードは必須項目です")
    private String code;
    private String remark;
}
