package co.handk.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Goods extends BaseEntity {

    private String name;

    private String englishName;

    private Long seriesId;

    private Long brandId;

    private Long categoryId;

    private Long makerId;

    private Integer status;

    private String description;

    private Integer isHot;

    private Integer sort;
}
