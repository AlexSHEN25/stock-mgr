package co.handk.common.model.dto.create;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StockOrderSubmitDTO {

    @NotNull(message = "伝票種別は必須です")
    private Integer orderType;

    private Integer sourceType;

    private String remark;

    @NotEmpty(message = "明細は必須です")
    @Valid
    private List<StockOrderSubmitItemDTO> items;
}
