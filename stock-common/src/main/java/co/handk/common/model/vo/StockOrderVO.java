package co.handk.common.model.vo;

import co.handk.common.annotation.SchemaField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockOrderVO extends BaseVO {
    @SchemaField(label = "\u4f1d\u7968\u756a\u53f7", order = 50)
    private String orderNo;
    @SchemaField(label = "\u4f1d\u7968\u7a2e\u5225", order = 50)
    private Integer orderType;

    @SchemaField(label = "\u5009\u5eabID", order = 50)
    private Long warehouseId;
    @SchemaField(label = "\u5143\u30c7\u30fc\u30bf\u7a2e\u5225", order = 50)
    private Integer sourceType;
    @SchemaField(label = "\u5143\u30c7\u30fc\u30bfID", order = 50)
    private Long sourceId;
    @SchemaField(label = "\u5408\u8a08\u6570\u91cf", order = 50)
    private Integer totalQty;
    @SchemaField(label = "\u5728\u5eab\u533a\u5206ID", order = 50)
    private Integer stockTypeId;
    @SchemaField(label = "\u72b6\u614b", order = 50)
    private Integer state;
    @SchemaField(label = "\u7533\u8acb\u8005ID", order = 50)
    private Long requesterId;
    @SchemaField(label = "\u7533\u8acb\u8005\u540d", order = 50)
    private String requesterName;
    @SchemaField(label = "\u64cd\u4f5c\u30e6\u30fc\u30b6\u30fcID", order = 50)
    private Long operatorId;
    @SchemaField(label = "\u64cd\u4f5c\u30e6\u30fc\u30b6\u30fc\u540d", order = 50)
    private String operatorName;
    @SchemaField(label = "\u5099\u8003", order = 50)
    private String remark;
    @SchemaField(label = "\u627f\u8a8d\u8005ID", order = 50)
    private Long approverId;
    @SchemaField(label = "\u627f\u8a8d\u8005\u540d", order = 50)
    private String approverName;
    @SchemaField(label = "\u627f\u8a8d\u65e5\u6642", order = 50)
    private LocalDateTime approveTime;
    @SchemaField(label = "\u5b8c\u4e86\u65e5\u6642", order = 50)
    private LocalDateTime finishTime;
}
