package co.handk.common.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupStockVO extends BaseVO {
    private Long batchId;
    private Long deptId;
    private String deptCode;
    private Long stockId;
    private Integer goodsId;
    private String goodsName;
    private Long skuId;
    private String skuCode;
    private Integer warehouseId;
    private String warehouseName;
    private Long stockTypeId;
    private String stockTypeName;
    private Integer allocatedQty;
    private Integer currentQty;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tokyo")
    private LocalDateTime saleDeadline;
    private Integer state;
    private String stateDesc;
}
