package co.handk.backend.entity;

import co.handk.schema.annotation.Schema;
import co.handk.schema.annotation.SchemaField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(resource = "requestForm", name = "申請書", group = "システム管理/申請書管理")
public class RequestForm extends BaseEntity {

    @SchemaField(title = "请求单号")
    private String bizNo;

    @SchemaField(title = "用户ID")
    private Long userId;

    @SchemaField(title = "用户名")
    private String username;

    @SchemaField(title = "申请部门ID")
    private Long deptId;

    @SchemaField(title = "申请部门名称")
    private String deptName;

    @SchemaField(title = "客户ID")
    private Long customerId;

    @SchemaField(title = "客户名称")
    private String customerName;

    @SchemaField(title = "出库仓库ID")
    private Long warehouseId;

    @SchemaField(title = "出库总数量")
    private Integer totalQty;

    @SchemaField(title = "请求书写入数量")
    private Integer requestQty;

    @SchemaField(title = "单据状态:0草稿1已提交2审核通过3已完成4已驳回5已取消")
    private Integer state;

    @SchemaField(title = "审核人ID")
    private Long approverId;

    @SchemaField(title = "审核人")
    private String approveName;

    @SchemaField(title = "审核时间")
    private LocalDateTime approveTime;

    @SchemaField(title = "审核备注")
    private String approveRemark;
}