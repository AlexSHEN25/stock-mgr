package co.handk.common.model.dto.create;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockOrderSubmitDTO {

    @NotNull(message = "伝票種別は必須です")
    private Integer orderType;

    private Integer sourceType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Tokyo")
    private LocalDateTime saleDeadline;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Tokyo")
    private LocalDate bizDate;

    private String remark;

    @NotEmpty(message = "明細は必須です")
    @Valid
    private List<StockOrderSubmitItemDTO> items;
}
