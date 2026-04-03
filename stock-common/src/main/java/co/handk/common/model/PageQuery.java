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

    @AssertTrue(message = "每页大小仅支持10、20、50")
    public boolean isPageSizeValid() {
        return pageSize != null
                && (pageSize.equals(DEFAULT_PAGE_SIZE) || pageSize.equals(PAGE_SIZE_20) || pageSize.equals(PAGE_SIZE_50));
    }
}
