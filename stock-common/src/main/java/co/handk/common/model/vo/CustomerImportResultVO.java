package co.handk.common.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CustomerImportResultVO {
    private int successCount;
    private int failureCount;
    private int createdCount;
    private int updatedCount;
    private List<CustomerImportRowResultVO> rows = new ArrayList<>();
}
