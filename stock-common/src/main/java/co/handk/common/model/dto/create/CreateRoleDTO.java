package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class CreateRoleDTO {

    @NotBlank(message = "ロール名は必須項目です")
    private String name;

    @NotBlank(message = "ロールコードは必須項目です")
    private String code;

    private String remark;
    private List<Long> permissionIds;
    // Frontend can send selected permission names; backend resolves to IDs.
    private List<String> permissionNames;
    private StatusEnum status;
}
