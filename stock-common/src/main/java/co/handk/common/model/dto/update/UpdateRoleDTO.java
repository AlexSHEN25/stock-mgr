package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleDTO {

    @NotNull(message = "IDは必須です")
    private Long id;

    @NotBlank(message = "ロール名は必須です")
    private String name;

    @NotBlank(message = "ロールコードは必須です")
    private String code;

    private String remark;
    private List<Long> permissionIds;
    // Frontend can send selected permission names; backend resolves to IDs.
    private List<String> permissionNames;
    private StatusEnum status;
}
