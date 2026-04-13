package co.handk.common.model.vo;

import lombok.Data;

@Data
public class GoodsVO extends BaseVO {
    private String name;
    private String englishName;
    private String skuCode;
    private Long seriesId;
    private Long brandId;
    private Long typeId;
    private Long makerId;
    private String description;
    private Integer isHot;
    private Integer status;
}
