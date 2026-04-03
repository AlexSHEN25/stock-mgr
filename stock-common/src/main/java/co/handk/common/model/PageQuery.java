package co.handk.common.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static co.handk.common.constant.PageQueryConstant.*;

/**
 * 通用分页请求基类
 */
@Data
public class PageQuery {

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Long pageNum = DEFAULT_PAGE_NUM;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    private Long pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 排序字段，仅支持 createTime / updateTime
     */
    private String sortBy = SORT_BY_UPDATE_TIME;

    /**
     * 排序方向，仅支持 asc / desc
     */
    private String sortOrder = SORT_ORDER_DESC;

    @AssertTrue(message = "每页大小仅支持10、20、50")
    public boolean isPageSizeValid() {
        return pageSize != null
                && (pageSize.equals(DEFAULT_PAGE_SIZE) || pageSize.equals(PAGE_SIZE_20) || pageSize.equals(PAGE_SIZE_50));
    }

    @AssertTrue(message = "排序字段仅支持createTime或updateTime")
    public boolean isSortByValid() {
        return SORT_BY_CREATE_TIME.equals(sortBy) || SORT_BY_UPDATE_TIME.equals(sortBy);
    }

    @AssertTrue(message = "排序方向仅支持asc或desc")
    public boolean isSortOrderValid() {
        return SORT_ORDER_ASC.equalsIgnoreCase(sortOrder) || SORT_ORDER_DESC.equalsIgnoreCase(sortOrder);
    }
}
