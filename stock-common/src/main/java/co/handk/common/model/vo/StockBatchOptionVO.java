package co.handk.common.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockBatchOptionVO {
    private Long batchId;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Tokyo")
    private LocalDate bizDate;
    private Integer availableQty;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tokyo")
    private LocalDateTime saleDeadline;
}
