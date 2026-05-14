package co.handk.common.model.dto.query;

import co.handk.common.enums.StatusEnum;
import co.handk.common.model.PageQuery;
import lombok.Data;

@Data
public class GoodsBundleQueryDTO extends PageQuery {

    private Long goodsId;
    private String goodsName;
    private String goodsEnglishName;
    private Long brandId;
    private Long seriesId;
    private Long categoryId;
    private Long makerId;
    private Integer isHot;
    private Integer goodsSort;
    private StatusEnum goodsStatus;

    private Long skuId;
    private String skuCode;
    private String skuName;
    private StatusEnum skuStatus;

    private Long specId;
    private String specName;
    private String specValue;

    private Long imageId;
    private String imageUrl;
}

