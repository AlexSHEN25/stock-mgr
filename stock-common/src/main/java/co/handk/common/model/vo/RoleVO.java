package co.handk.common.model.vo;

import lombok.Data;

@Data
public class RoleVO extends BaseVO {
    private String name;
    private String code;
    private String remark;
    private Integer status;
    private String statusDesc;
}
