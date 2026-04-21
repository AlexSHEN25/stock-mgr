package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(resource = "priceRecord", name = "価格履歴", group = "システム管理/在庫管理")
public class PriceRecord extends BaseEntity {

    @SchemaField(title = "商品ID")
    private Long goodsId;

    @SchemaField(title = "商品名称")
    private String goodsName;

    @SchemaField(title = "英文品名")
    private String englishName;

    @SchemaField(title = "SKU ID")
    private Long skuId;

    @SchemaField(title = "商品品番")
    private String skuCode;

    @SchemaField(title = "更新前单价")
    private BigDecimal oldPrice;

    @SchemaField(title = "更新后单价")
    private BigDecimal newPrice;
    @SchemaField(title = "币种")
    private String currency;

    @SchemaField(title = "折扣率")
    private BigDecimal discount;

    @SchemaField(title = "价格更新时间")
    private LocalDateTime priceUpdateTime;

    @SchemaField(title = "操作人id")
    private Long operatorId;

    @SchemaField(title = "操作人名")
    private String operatorName;
}