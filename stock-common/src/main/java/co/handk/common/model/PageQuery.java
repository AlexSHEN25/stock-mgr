package co.handk.common.model;

import lombok.Data;

/**
 * 通用分页请求基类
 */
@Data
public class PageQuery {

    /**
     * 页码
     */
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    private Long pageSize = 10L;
}