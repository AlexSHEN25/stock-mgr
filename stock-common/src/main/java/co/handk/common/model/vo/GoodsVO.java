package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

@Data
public class GoodsVO extends BaseVO {
    @SchemaField(label = "\u540d\u79f0", order = 50)
    private String name;
    @SchemaField(label = "\u82f1\u8a9e\u540d", order = 50)
    private String englishName;
    @SchemaField(label = "\u30d6\u30e9\u30f3\u30c9ID", order = 50)
    private Long brandId;
    @SchemaField(label = "\u30d6\u30e9\u30f3\u30c9\u540d", order = 50)
    private String brandName;
    @SchemaField(label = "\u30b7\u30ea\u30fc\u30baID", order = 50)
    private Long seriesId;
    @SchemaField(label = "\u30b7\u30ea\u30fc\u30ba\u540d", order = 50)
    private String seriesName;
    @SchemaField(label = "\u30ab\u30c6\u30b4\u30eaID", order = 50)
    private Long categoryId;
    @SchemaField(label = "\u30ab\u30c6\u30b4\u30ea\u540d", order = 50)
    private String categoryName;
    @SchemaField(label = "\u30e1\u30fc\u30ab\u30fcID", order = 50)
    private Long makerId;
    @SchemaField(label = "\u30e1\u30fc\u30ab\u30fc\u540d", order = 50)
    private String makerName;
    @SchemaField(label = "\u8aac\u660e", order = 50)
    private String description;
    @SchemaField(label = "\u6ce8\u76ee\u30d5\u30e9\u30b0", order = 50)
    private Integer isHot;
    @SchemaField(label = "\u4e26\u3073\u9806", order = 50)
    private Integer sort;
    @SchemaField(label = "\u72b6\u614b\u30b3\u30fc\u30c9", order = 40)
    private Integer status;
    @SchemaField(label = "\u72b6\u614b", order = 41)
    private String statusDesc;
}
