package co.handk.common.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoleVO extends BaseVO {
    private String name;
    private String code;
    private String remark;
    private List<Long> permissionIds;
    private List<String> permissionNames;
    private Integer status;
    private String statusDesc;
}
