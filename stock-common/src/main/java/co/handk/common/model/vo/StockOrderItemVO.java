package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockOrderItemVO extends BaseVO {
    @SchemaField(label = "\u4f1d\u7968ID", order = 50)
    private Long orderId;
    @SchemaField(label = "\u5546\u54c1ID", order = 50)
    private Long goodsId;
    @SchemaField(label = "SKU ID", order = 50)
    private Long skuId;
    @SchemaField(label = "SKU\u30b3\u30fc\u30c9", order = 50)
    private String skuCode;
    @SchemaField(label = "\u5546\u54c1\u540d", order = 50)
    private String goodsName;
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

    @SchemaField(label = "\u5728\u5eab\u533a\u5206ID", order = 50)
    private Long stockTypeId;
    @SchemaField(label = "\u5728\u5eab\u533a\u5206", order = 50)
    private String stockTypeName;

    @SchemaField(label = "\u30e1\u30fc\u30ab\u30fcID", order = 50)
    private Long makerId;
    @SchemaField(label = "\u30e1\u30fc\u30ab\u30fc\u540d", order = 50)
    private String makerName;
    @SchemaField(label = "\u5909\u66f4\u524d\u6570\u91cf", order = 50)
    private Integer beforeQty;
    @SchemaField(label = "\u5909\u52d5\u6570\u91cf", order = 50)
    private Integer changeQty;
    @SchemaField(label = "\u5909\u66f4\u5f8c\u6570\u91cf", order = 50)
    private Integer afterQty;
    @SchemaField(label = "\u4fa1\u683c", order = 50)
    private BigDecimal price;
    @SchemaField(label = "\u901a\u8ca8", order = 50)
    private String currency;
    @SchemaField(label = "\u5099\u8003", order = 50)
    private String remark;
}
