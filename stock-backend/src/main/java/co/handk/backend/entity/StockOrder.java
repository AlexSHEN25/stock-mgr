package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(resource = "stockOrder", name = "在庫業務伝票", group = "システム管理/在庫管理")
public class StockOrder extends BaseEntity {

    @SchemaField(title = "库存单号")
    private String orderNo;

    @SchemaField(title = "单据类型:1 入库 2 出库 3 调整 4 盘点 5 调拨 6 退货")
    private Integer orderType;

    @SchemaField(title = "库存商品分类ID")
    private Integer typeId;

    @SchemaField(title = "仓库ID")
    private Long warehouseId;

    @SchemaField(title = "来源类型:1订单2退货3请求单4手动")
    private Integer sourceType;

    @SchemaField(title = "来源ID")
    private Long sourceId;

    @SchemaField(title = "总数量")
    private Integer totalQty;

    @SchemaField(title = "单据状态:0草稿1审核中2完成3取消")
    private Integer state;

    @SchemaField(title = "申请人ID")
    private Long requesterId;

    @SchemaField(title = "申请人")
    private String requesterName;

    @SchemaField(title = "操作人ID")
    private Long operatorId;

    @SchemaField(title = "操作人")
    private String operatorName;

    @SchemaField(title = "备注")
    private String remark;

    @SchemaField(title = "审核人ID")
    private Long approverId;

    @SchemaField(title = "审核人")
    private String approverName;

    @SchemaField(title = "审核时间")
    private LocalDateTime approveTime;

    @SchemaField(title = "版本控制")
    private Long version;

    @SchemaField(title = "完成时间")
    private LocalDateTime finishTime;
}