package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(resource = "requestItem", name = "申請商品明細", group = "システム管理/申請書管理")
public class RequestItem extends BaseEntity {

    @SchemaField(title = "请求单ID")
    private Long requestId;

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

    @SchemaField(title = "类型ID")
    private Long categoryId;

    @SchemaField(title = "类型名称")
    private String categoryName;

    @SchemaField(title = "商品分类ID")
    private Long typeId;

    @SchemaField(title = "商品分类名称")
    private String typeName;

    @SchemaField(title = "厂家ID")
    private Long makerId;

    @SchemaField(title = "厂家名称")
    private String makerName;

    @SchemaField(title = "仓库ID")
    private Long warehouseId;

    @SchemaField(title = "单价")
    private BigDecimal price;
    @SchemaField(title = "币种")
    private String currency;

    @SchemaField(title = "折扣率")
    private BigDecimal discount;

    @SchemaField(title = "申请数量")
    private Integer requestQty;

    @SchemaField(title = "审核通过数量")
    private Integer approveQty;

    @SchemaField(title = "实际出库数量")
    private Integer outQty;

    @SchemaField(title = "库存流水ID")
    private Long stockRecordId;

    @SchemaField(title = "备注")
    private String remark;
}