package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePermissionDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private String name;
    private String code;
    private String module;
    private Integer type;
    private Long parentId;
    private String path;
    private Integer sort;
    private String icon;
    private String component;
    private StatusEnum status;
}
