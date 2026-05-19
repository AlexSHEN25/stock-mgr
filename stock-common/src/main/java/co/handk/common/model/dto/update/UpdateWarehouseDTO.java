package co.handk.common.model.dto.update;

import co.handk.common.enums.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWarehouseDTO {
    @NotNull(message = "IDは必須項目です")
    private Long id;

    private String name;
    private String code;
    private String address;
    private Long managerId;
    private StatusEnum status;
}
