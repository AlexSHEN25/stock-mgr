package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class SeriesVO extends BaseVO {
    @SchemaField(label = "\u540d\u79f0", order = 50)
    private String name;
    @SchemaField(label = "\u82f1\u8a9e\u540d", order = 50)
    private String englishName;

    @SchemaField(label = "\u30d6\u30e9\u30f3\u30c9ID", order = 50)
    private Long brandId;
    @SchemaField(label = "\u30d6\u30e9\u30f3\u30c9\u540d", order = 50)
    private String brandName;
    @SchemaField(label = "\u5185\u5bb9", order = 50)
    private String content;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
