package co.handk.common.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class GoodsWorkbenchDetailVO {
    private GoodsVO goods;
    private List<GoodsSkuVO> skus;
    private List<GoodsSkuSpecVO> specs;
    private List<GoodsImageVO> images;
    private List<GoodsLevelPriceVO> memberPrices;
    private Long specId;
    private String specName;
    private String specValue;
    private String imageUrl;
}
