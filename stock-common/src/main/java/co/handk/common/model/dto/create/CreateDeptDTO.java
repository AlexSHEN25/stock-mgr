package co.handk.common.model.dto.create;

import lombok.Data;

@Data
public class CreateDeptDTO {
    private String name;
    private String code;
    private Long leaderId;
    private Integer sort;
}
