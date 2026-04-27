package co.handk.common.model.dto.create;

import lombok.Data;

@Data
public class CreateWarehouseDTO {

    private String name;
    private String code;
    private String address;
    private Long managerId;
}
