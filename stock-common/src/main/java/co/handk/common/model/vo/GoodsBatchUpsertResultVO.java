package co.handk.common.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GoodsBatchUpsertResultVO {

    private Integer totalCount;
    private Integer successCount;
    private Integer createdCount;
    private Integer updatedCount;
    private Integer failureCount;
    private String errorReportFileName;
    private String errorReportBase64;
    private List<GoodsBatchUpsertRowResultVO> rows = new ArrayList<>();
}
