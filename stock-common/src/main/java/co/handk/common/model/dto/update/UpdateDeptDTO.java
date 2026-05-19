package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDeptDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotBlank(message = "名称は必須項目です")
    private String name;
    private String code;
    private Long leaderId;
    private Integer sort;
    private StatusEnum status;
}
