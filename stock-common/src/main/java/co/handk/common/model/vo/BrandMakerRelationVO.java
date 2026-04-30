package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class BrandMakerRelationVO extends BaseVO {
    @SchemaField(label = "\u30d6\u30e9\u30f3\u30c9ID", order = 50)
    private Long brandId;
    @SchemaField(label = "\u30d6\u30e9\u30f3\u30c9\u540d", order = 50)
    private String brandName;
    @SchemaField(label = "\u30e1\u30fc\u30ab\u30fcID", order = 50)
    private Long makerId;
    @SchemaField(label = "\u30e1\u30fc\u30ab\u30fc\u540d", order = 50)
    private String makerName;
}
