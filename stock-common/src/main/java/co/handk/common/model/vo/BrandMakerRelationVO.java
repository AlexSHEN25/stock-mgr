package co.handk.common.model.vo;

import lombok.Data;

@Data
public class BrandMakerRelationVO extends BaseVO {
    private Long brandId;
    private String brandName;
    private Long makerId;
    private String makerName;
}
