package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestFormVO extends BaseVO {
    @SchemaField(label = "\u696d\u52d9\u756a\u53f7", order = 50)
    private String bizNo;
    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long userId;
    @SchemaField(label = "\u30e6\u30fc\u30b6\u30fc\u540d", order = 50)
    private String username;
    @SchemaField(label = "\u90e8\u7f72ID", order = 50)
    private Long deptId;
    @SchemaField(label = "\u90e8\u7f72\u540d", order = 50)
    private String deptName;
    @SchemaField(label = "\u9867\u5ba2ID", order = 50)
    private Long customerId;
    @SchemaField(label = "\u9867\u5ba2\u540d", order = 50)
    private String customerName;
    @SchemaField(label = "\u5009\u5eabID", order = 50)
    private Long warehouseId;
    @SchemaField(label = "\u5408\u8a08\u6570\u91cf", order = 50)
    private Integer totalQty;
    @SchemaField(label = "\u7533\u8acb\u6570\u91cf", order = 50)
    private Integer requestQty;
    @SchemaField(label = "\u72b6\u614b", order = 50)
    private Integer state;
    @SchemaField(label = "\u627f\u8a8d\u8005ID", order = 50)
    private Long approverId;
    @SchemaField(label = "\u627f\u8a8d\u8005\u540d", order = 50)
    private String approveName;
    @SchemaField(label = "\u627f\u8a8d\u65e5\u6642", order = 50)
    private LocalDateTime approveTime;
    @SchemaField(label = "\u627f\u8a8d\u5099\u8003", order = 50)
    private String approveRemark;
}
