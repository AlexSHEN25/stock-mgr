package co.handk.common.model.dto;

import lombok.Data;

@Data
public class DeptDTO {

    private Long id;

    private Long parentId;
    private String name;
    private String code;
    private Long leaderId;
    private Integer sort;
    private Integer status;
}
