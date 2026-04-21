package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(resource = "stockRecord", name = "在庫履歴", group = "システム管理/在庫管理")
public class StockRecord extends BaseEntity {

    @SchemaField(title = "业务单号")
    private String bizNo;

    @SchemaField(title = "库存业务单ID")
    private Long orderId;

    @SchemaField(title = "库存单明细ID")
    private Long orderItemId;

    @SchemaField(title = "库存表ID")
    private Long stockId;

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

    @SchemaField(title = "仓库ID")
    private Long warehouseId;

    @SchemaField(title = "变更前库存")
    private Integer beforeQty;

    @SchemaField(title = "变化数量")
    private Integer changeQty;

    @SchemaField(title = "变更后库存")
    private Integer afterQty;

    @SchemaField(title = "单据类型:1 入库 2 出库 3 调整 4 盘点 5 调拨 6 退货")
    private Integer orderType;

    @SchemaField(title = "来源类型:1订单2退货3请求单4手动")
    private Integer sourceType;

    @SchemaField(title = "单价")
    private BigDecimal price;
    @SchemaField(title = "币种")
    private String currency;

    @SchemaField(title = "价格最后更新时间")
    private LocalDateTime priceUpdateTime;

    @SchemaField(title = "客户ID")
    private Long customerId;

    @SchemaField(title = "客户名称")
    private String customerName;

    @SchemaField(title = "申请人id")
    private Long requesterId;

    @SchemaField(title = "申请人名")
    private String requesterName;

    @SchemaField(title = "操作人id")
    private Long operatorId;

    @SchemaField(title = "操作人名")
    private String operatorName;

    @SchemaField(title = "备注")
    private String remark;
}