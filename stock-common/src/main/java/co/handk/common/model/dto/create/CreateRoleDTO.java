package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import co.handk.common.enums.StatusEnum;
import lombok.Data;

@Data
public class CreateRoleDTO {

    @NotBlank(message = "必須項目です")
    private String name;
    @NotBlank(message = "必須項目です")
    private String code;
    private String remark;
    private StatusEnum status;

}
