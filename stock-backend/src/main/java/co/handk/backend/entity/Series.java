package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Series extends BaseEntity {

    private String name;

    private String englishName;

    private Long brandId;

    private String content;

    private Integer status;
}
