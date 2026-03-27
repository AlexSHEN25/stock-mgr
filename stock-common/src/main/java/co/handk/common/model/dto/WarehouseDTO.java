package co.handk.common.model.dto;

import lombok.Data;

@Data
public class WarehouseDTO {

    private Long id;

    private String name;
    private String code;
    private String address;
    private Long managerId;
    private Integer status;
}
