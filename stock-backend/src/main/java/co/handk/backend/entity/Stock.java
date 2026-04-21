package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(resource = "stock", name = "在庫商品", group = "システム管理/在庫管理")
public class Stock extends BaseEntity {

    @SchemaField(title = "商品ID")
    private Integer goodsId;

    @SchemaField(title = "商品名称")
    private String goodsName;

    @SchemaField(title = "SKU ID")
    private Long skuId;

    @SchemaField(title = "商品品番")
    private String skuCode;

    @SchemaField(title = "仓库ID")
    private Integer warehouseId;

    @SchemaField(title = "实际库存数量")
    private Integer currentQty;

    @SchemaField(title = "已被锁定库存数量")
    private Integer lockQty;

    @SchemaField(title = "单价")
    private BigDecimal price;
    @SchemaField(title = "币种")
    private String currency;

    @SchemaField(title = "价格最后更新时间")
    private LocalDateTime priceUpdateTime;

    @SchemaField(title = "状态")
    private Integer status;

    @SchemaField(title = "版本控制")
    private Long version;
}