package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;

import lombok.Data;

@Data
public class CreateWarehouseDTO {

    private String name;
    private String code;
    private String address;
    private Long managerId;
    private StatusEnum status;
}
