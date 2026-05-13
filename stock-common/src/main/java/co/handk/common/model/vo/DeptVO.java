package co.handk.common.model.vo;

import lombok.Data;

@Data
public class DeptVO extends BaseVO {
    private String name;
    private String code;
    private Long leaderId;
    private String leaderName;
    private Integer sort;
    private Integer status;
    private String statusDesc;
}
