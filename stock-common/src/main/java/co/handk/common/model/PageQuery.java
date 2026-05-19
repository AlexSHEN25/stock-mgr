package co.handk.common.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static co.handk.common.constant.PageQueryConstant.*;

@Data
public class PageQuery {

    private Long id;

    @NotNull(message = "ページ番号は必須です")
    @Min(value = 1, message = "ページ番号は1以上である必要があります")
    private Long pageNum = DEFAULT_PAGE_NUM;

    @NotNull(message = "ページサイズは必須です")
    private Long pageSize = DEFAULT_PAGE_SIZE;

    private String sortBy = SORT_BY_UPDATE_TIME;

    private String sortOrder = SORT_ORDER_DESC;

    @AssertTrue(message = "ページサイズは10、20、50のみ指定できます")
    public boolean isPageSizeValid() {
        return pageSize != null
                && (pageSize.equals(DEFAULT_PAGE_SIZE) || pageSize.equals(PAGE_SIZE_20) || pageSize.equals(PAGE_SIZE_50));
    }

    @AssertTrue(message = "ソート項目はcreateTimeまたはupdateTimeのみ指定できます")
    public boolean isSortByValid() {
        return SORT_BY_CREATE_TIME.equals(sortBy) || SORT_BY_UPDATE_TIME.equals(sortBy);
    }

    @AssertTrue(message = "ソート順はascまたはdescのみ指定できます")
    public boolean isSortOrderValid() {
        return SORT_ORDER_ASC.equalsIgnoreCase(sortOrder) || SORT_ORDER_DESC.equalsIgnoreCase(sortOrder);
    }
}
