package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(resource = "goodsSku", name = "商品SKU", group = "システム管理/商品管理")
public class GoodsSku extends BaseEntity {

    @SchemaField(title = "商品ID")
    private Long goodsId;

    @SchemaField(title = "SKU编码/商品品番")
    private String skuCode;

    @SchemaField(title = "SKU展示名称")
    private String skuName;

    @SchemaField(title = "销售价")
    private BigDecimal price;
    @SchemaField(title = "币种")
    private String currency;

    @SchemaField(title = "成本价")
    private BigDecimal costPrice;

    @SchemaField(title = "待更新价格")
    private BigDecimal updatePrice;

    @SchemaField(title = "价格更新时间")
    private LocalDateTime priceUpdateTime;

    @SchemaField(title = "条码")
    private String barcode;

    @SchemaField(title = "重量")
    private BigDecimal weight;

    @SchemaField(title = "体积")
    private BigDecimal volume;

    @SchemaField(title = "状态")
    private Integer status;
}