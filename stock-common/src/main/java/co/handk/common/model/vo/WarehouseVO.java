package co.handk.common.model.vo;

import lombok.Data;

@Data
public class WarehouseVO extends BaseVO {
    private String name;
    private String code;
    private String address;
    private Long managerId;
    private Integer status;
}
