package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDeptDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    private Long parentId;
    private String name;
    private String code;
    private Long leaderId;
    private Integer sort;
    private StatusEnum status;
}
