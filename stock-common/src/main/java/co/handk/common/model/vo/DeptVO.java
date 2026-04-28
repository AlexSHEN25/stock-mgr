package co.handk.common.model.vo;

import lombok.Data;

@Data
public class DeptVO extends BaseVO {
    private Long parentId;
    private String name;
    private String code;
    private Long leaderId;
    private Integer sort;
    private Integer status;
    private String statusDesc;
}
