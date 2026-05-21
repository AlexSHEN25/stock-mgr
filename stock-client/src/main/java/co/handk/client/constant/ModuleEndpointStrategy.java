package co.handk.client.constant;

import static co.handk.client.constant.AppConstants.ApiPath;
import static co.handk.client.constant.AppConstants.Module;

import java.util.HashMap;
import java.util.Map;

public final class ModuleEndpointStrategy {

    private ModuleEndpointStrategy() {
    }

    private enum HttpMethod {
        GET, POST
    }

    private record EndpointConfig(HttpMethod pageMethod, String pagePath, boolean updateContainsIdInPath) {}

    private static final Map<String, EndpointConfig> CONFIGS = new HashMap<>();

    static {
        CONFIGS.put(Module.USER, new EndpointConfig(HttpMethod.POST, ApiPath.USER_PAGE, true));
    }

    private static EndpointConfig configOf(String moduleKey) {
        return CONFIGS.getOrDefault(moduleKey,
                new EndpointConfig(HttpMethod.GET, "/" + moduleKey + ApiPath.PAGE_SUFFIX, false));
    }

    public static String pagePath(String moduleKey) {
        return configOf(moduleKey).pagePath;
    }

    public static boolean pageUsesPost(String moduleKey) {
        return configOf(moduleKey).pageMethod == HttpMethod.POST;
    }

    public static String updatePath(String moduleKey, String id) {
        if (configOf(moduleKey).updateContainsIdInPath) {
            return "/user/" + id;
        }
        return "/" + moduleKey;
    }

    public static boolean updateContainsIdInPath(String moduleKey) {
        return configOf(moduleKey).updateContainsIdInPath;
    }
}
