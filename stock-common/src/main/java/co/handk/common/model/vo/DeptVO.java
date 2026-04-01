package co.handk.common.model.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DeptVO {

    private Long id;

    private Long parentId;
    private String name;
    private String code;
    private Long leaderId;
    private Integer sort;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
