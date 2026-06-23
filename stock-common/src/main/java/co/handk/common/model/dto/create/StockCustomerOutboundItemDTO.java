package co.handk.common.model.dto.create;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class StockCustomerOutboundItemDTO {

    private Long customerId;

    private String customerName;

    @JsonAlias({"outQty", "outboundQty", "outboundQuantity", "splitQty", "customerQty", "deliveryQty", "qty"})
    private Integer quantity;

    private String remark;
}
