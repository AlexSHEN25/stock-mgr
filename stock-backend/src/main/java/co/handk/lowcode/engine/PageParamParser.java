package co.handk.lowcode.engine;

import java.util.Map;

/**
 * 分页参数解析
 */
public class PageParamParser {

    public static long getPage(Map<String, Object> params) {
        return parse(params.get("page"), 1);
    }

    public static long getSize(Map<String, Object> params) {
        return parse(params.get("size"), 10);
    }

    private static long parse(Object val, long def) {
        if (val == null) return def;
        return Long.parseLong(val.toString());
    }
}