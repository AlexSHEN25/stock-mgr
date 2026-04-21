package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(resource = "stockOrderItem", name = "在庫伝票明細", group = "システム管理/在庫管理")
public class StockOrderItem extends BaseEntity {

    @SchemaField(title = "库存单ID")
    private Long orderId;

    @SchemaField(title = "商品ID")
    private Long goodsId;

    @SchemaField(title = "SKU ID")
    private Long skuId;

    @SchemaField(title = "商品品番")
    private String skuCode;

    @SchemaField(title = "商品名称")
    private String goodsName;

    @SchemaField(title = "英文品名")
    private String englishName;

    @SchemaField(title = "品牌ID")
    private Long brandId;

    @SchemaField(title = "品牌名称")
    private String brandName;

    @SchemaField(title = "系列ID")
    private Long seriesId;

    @SchemaField(title = "系列名称")
    private String seriesName;

    @SchemaField(title = "库存商品分类ID")
    private Long typeId;

    @SchemaField(title = "库存商品分类名称")
    private String typeName;

    @SchemaField(title = "厂家ID")
    private Long makerId;

    @SchemaField(title = "厂家名称")
    private String makerName;

    @SchemaField(title = "变更前库存")
    private Integer beforeQty;

    @SchemaField(title = "变化数量")
    private Integer changeQty;

    @SchemaField(title = "变更后库存")
    private Integer afterQty;

    @SchemaField(title = "单价")
    private BigDecimal price;
    @SchemaField(title = "币种")
    private String currency;

    @SchemaField(title = "remark")
    private String remark;
}