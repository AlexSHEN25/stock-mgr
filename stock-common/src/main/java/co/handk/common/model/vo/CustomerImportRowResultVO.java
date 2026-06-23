package co.handk.common.model.vo;

import lombok.Data;

@Data
public class CustomerImportRowResultVO {
    private Integer rowNo;
    private Boolean success;
    private String action;
    private Long customerId;
    private String customerCode;
    private String message;
}
