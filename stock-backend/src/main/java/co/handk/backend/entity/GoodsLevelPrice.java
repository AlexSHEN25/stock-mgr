package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(resource = "goodsLevelPrice", name = "顧客ランク別価格", group = "システム管理/顧客管理")
public class GoodsLevelPrice extends BaseEntity {

    @SchemaField(title = "商品ID")
    private Long goodsId;

    @SchemaField(title = "SKU ID")
    private Long skuId;

    @SchemaField(title = "商品品番")
    private String skuCode;

    @SchemaField(title = "客户等级ID")
    private Long levelId;

    @SchemaField(title = "等级专属价格")
    private BigDecimal price;
    @SchemaField(title = "币种")
    private String currency;

    @SchemaField(title = "等级折扣率(可选)")
    private BigDecimal discount;

    @SchemaField(title = "生效时间")
    private LocalDateTime effectiveTime;

    @SchemaField(title = "失效时间")
    private LocalDateTime expireTime;

    @SchemaField(title = "状态")
    private Integer status;
}