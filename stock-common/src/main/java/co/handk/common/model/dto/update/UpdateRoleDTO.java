package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotBlank(message = "名称は必須項目です")
    private String name;
    @NotBlank(message = "ロールコードは必須項目です")
    private String code;
    private String remark;
    private StatusEnum status;
}
