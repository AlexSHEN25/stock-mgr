package co.handk.client.constant;

import java.util.HashMap;
import java.util.Map;

public final class ModuleEndpointStrategy {

    private static final String PATH_PREFIX = "/";

    private ModuleEndpointStrategy() {
    }

    private enum HttpMethod {
        GET, POST
    }

    private record EndpointConfig(
            HttpMethod pageMethod,
            String pagePath,
            String createPath,
            String detailPathTemplate,
            String updatePathTemplate,
            String deletePathTemplate,
            String batchDeletePath) {
    }

    private static final Map<String, EndpointConfig> CONFIGS = new HashMap<>();

    static {
        CONFIGS.put(AppConstants.Module.USER, new EndpointConfig(
                HttpMethod.POST,
                AppConstants.ApiPath.USER_PAGE,
                PATH_PREFIX + AppConstants.Module.USER,
                PATH_PREFIX + AppConstants.Module.USER + "/{id}",
                PATH_PREFIX + AppConstants.Module.USER + "/{id}",
                PATH_PREFIX + AppConstants.Module.USER + "/{id}",
                null
        ));
        CONFIGS.put(AppConstants.Module.STOCK, new EndpointConfig(
                HttpMethod.GET,
                PATH_PREFIX + AppConstants.Module.STOCK + AppConstants.ApiPath.PAGE_SUFFIX,
                AppConstants.ApiPath.STOCK_INBOUND,
                PATH_PREFIX + AppConstants.Module.STOCK + "/{id}",
                PATH_PREFIX + AppConstants.Module.STOCK,
                PATH_PREFIX + AppConstants.Module.STOCK + "/{id}",
                PATH_PREFIX + AppConstants.Module.STOCK + "/batch"
        ));
        CONFIGS.put(AppConstants.Module.BRAND_HIERARCHY, new EndpointConfig(
                HttpMethod.GET,
                AppConstants.ApiPath.BRAND_HIERARCHY_PAGE,
                AppConstants.ApiPath.BRAND_HIERARCHY,
                AppConstants.ApiPath.BRAND_HIERARCHY + "/{id}",
                AppConstants.ApiPath.BRAND_HIERARCHY,
                AppConstants.ApiPath.BRAND_HIERARCHY + "/{id}",
                null
        ));
        CONFIGS.put(AppConstants.Module.REQUEST_ITEM, new EndpointConfig(
                HttpMethod.GET,
                AppConstants.ApiPath.REQUEST_ITEM_CART_PAGE,
                AppConstants.ApiPath.REQUEST_FORM_WITH_ITEMS,
                PATH_PREFIX + AppConstants.Module.REQUEST_ITEM + "/{id}",
                PATH_PREFIX + AppConstants.Module.REQUEST_ITEM,
                PATH_PREFIX + AppConstants.Module.REQUEST_ITEM + "/{id}",
                null
        ));
    }

    private static EndpointConfig defaultConfig(String moduleKey) {
        String basePath = PATH_PREFIX + moduleKey;
        return new EndpointConfig(
                HttpMethod.GET,
                basePath + AppConstants.ApiPath.PAGE_SUFFIX,
                basePath,
                basePath + "/{id}",
                basePath,
                basePath + "/{id}",
                null
        );
    }

    private static EndpointConfig configOf(String moduleKey) {
        return CONFIGS.getOrDefault(moduleKey, defaultConfig(moduleKey));
    }

    private static String resolveTemplate(String template, String id) {
        return template.replace("{id}", id);
    }

    public static String pagePath(String moduleKey) {
        return configOf(moduleKey).pagePath;
    }

    public static boolean pageUsesPost(String moduleKey) {
        return configOf(moduleKey).pageMethod == HttpMethod.POST;
    }

    public static String createPath(String moduleKey) {
        return configOf(moduleKey).createPath;
    }

    public static String detailPath(String moduleKey, String id) {
        return resolveTemplate(configOf(moduleKey).detailPathTemplate, id);
    }

    public static String updatePath(String moduleKey, String id) {
        return resolveTemplate(configOf(moduleKey).updatePathTemplate, id);
    }

    public static String deletePath(String moduleKey, String id) {
        return resolveTemplate(configOf(moduleKey).deletePathTemplate, id);
    }

    public static String batchDeletePath(String moduleKey) {
        return configOf(moduleKey).batchDeletePath;
    }
}
