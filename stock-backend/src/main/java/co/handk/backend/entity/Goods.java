package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Goods extends BaseEntity {

    private String name;

    private String englishName;

    private Long brandId;

    private Long seriesId;

    private Long categoryId;

    private Long makerId;

    private String description;

    private Integer isHot;

    private Integer status;

    private Integer sort;
}
