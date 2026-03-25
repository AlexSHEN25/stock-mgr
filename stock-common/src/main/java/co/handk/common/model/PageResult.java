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
     * 当前页数据列表
     */
    private List<T> records;

    private PageResult(Long total, Long pageNum, Long pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
    }

    public static <T> PageResult<T> build(Long total, Long pageNum, Long pageSize, List<T> records) {
        return new PageResult<>(total, pageNum, pageSize, records);
    }
}