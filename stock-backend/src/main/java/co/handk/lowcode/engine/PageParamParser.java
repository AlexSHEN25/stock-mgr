package co.handk.lowcode.engine;

import co.handk.common.constant.PageQueryConstant;

import java.util.Map;

/**
 * 分页参数解析
 */
public class PageParamParser {

    public static long getPage(Map<String, Object> params) {
        return parse(params.get("page"), PageQueryConstant.DEFAULT_PAGE_NUM);
    }

    public static long getSize(Map<String, Object> params) {
        return parse(params.get("size"), PageQueryConstant.DEFAULT_PAGE_SIZE);
    }

    private static long parse(Object val, long def) {
        if (val == null) return def;
        return Long.parseLong(val.toString());
    }
}