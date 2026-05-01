package co.handk.common.model.vo;

import lombok.Data;

@Data
public class GoodsVO extends BaseVO {
    private String name;
    private String englishName;
    private Long brandId;
    private String brandName;
    private Long seriesId;
    private String seriesName;
    private Long categoryId;
    private String categoryName;
    private Long makerId;
    private String makerName;
    private String description;
    private Integer isHot;
    private Integer sort;
    private Integer status;
    private String statusDesc;
}
