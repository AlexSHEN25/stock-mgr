package co.handk.common.model;

import lombok.Getter;

import java.util.List;

/**
 * 通用分页返回对象
 *
 * @param <T> 分页数据中每一条记录的类型
 */
@Getter
public class PageResult<T> {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 总页数
     */
    private Long totalPages;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    private PageResult(Long total, Long pageNum, Long pageSize, Long totalPages, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.records = records;
    }

    public static <T> PageResult<T> build(Long total, Long pageNum, Long pageSize, List<T> records) {
        long safeTotal = total == null ? 0L : total;
        long safePageSize = (pageSize == null || pageSize <= 0) ? 0L : pageSize;
        long pages = safePageSize == 0 ? 0L : (safeTotal + safePageSize - 1) / safePageSize;
        return new PageResult<>(safeTotal, pageNum, pageSize, pages, records);
    }
}
