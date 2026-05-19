package co.handk.common.model.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePermissionDTO {

    @NotBlank(message = "名称は必須項目です")
    private String name;
    @NotBlank(message = "権限コードは必須項目です")
    private String code;
    private String module;
    private Integer type;
    private Long parentId;
    private String path;
    private Integer sort;
    private String icon;
    private String component;
}
