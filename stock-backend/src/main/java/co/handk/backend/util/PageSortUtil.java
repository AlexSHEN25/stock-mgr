package co.handk.backend.util;

import co.handk.common.model.PageQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import static co.handk.common.constant.PageQueryConstant.SORT_BY_CREATE_TIME;
import static co.handk.common.constant.PageQueryConstant.SORT_ORDER_ASC;

/**
 * 分页排序工具：仅支持按创建时间/更新时间排序。
 */
public final class PageSortUtil {

    private PageSortUtil() {
    }

    public static <T> void applyTimeSort(
            LambdaQueryWrapper<T> wrapper,
            PageQuery query,
            SFunction<T, ?> createTimeColumn,
            SFunction<T, ?> updateTimeColumn) {
        boolean asc = SORT_ORDER_ASC.equalsIgnoreCase(query.getSortOrder());
        if (SORT_BY_CREATE_TIME.equals(query.getSortBy())) {
            wrapper.orderBy(true, asc, createTimeColumn);
            return;
        }
        wrapper.orderBy(true, asc, updateTimeColumn);
    }
}
