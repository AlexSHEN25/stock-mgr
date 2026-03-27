package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Warehouse extends BaseEntity {

    private String name;

    private String code;

    private String address;

    private Long managerId;

    private Integer status;
}
