package co.handk.common.model.dto.create;

import co.handk.common.enums.StatusEnum;

import lombok.Data;

@Data
public class CreateRoleDTO {

    private String name;
    private String code;
    private String remark;
    private StatusEnum status;
}
