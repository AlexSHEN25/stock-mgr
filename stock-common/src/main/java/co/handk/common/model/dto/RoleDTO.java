package co.handk.common.model.dto;

import lombok.Data;

@Data
public class RoleDTO {

    private Long id;

    private String name;
    private String code;
    private String remark;
    private Integer status;
}
