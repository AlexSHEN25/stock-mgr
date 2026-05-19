package co.handk.common.model.dto.update;

import jakarta.validation.constraints.NotBlank;
import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMakerDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    @NotBlank(message = "名称は必須項目です")
    private String name;
    private StatusEnum status;
}
