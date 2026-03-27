package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Dept extends BaseEntity {

    private Long parentId;

    private String name;

    private String code;

    private Long leaderId;

    private Integer sort;

    private Integer status;
}
