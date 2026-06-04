package co.handk.client.service;

import co.handk.client.constant.AppConstants.ApiPath;
import co.handk.client.util.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserAccountService {

    public JSONObject logout() throws Exception {
        return new JSONObject(ApiClient.post(ApiPath.USER_LOGOUT, "{}"));
    }

    public JSONObject changePassword(Long userId, String password) throws Exception {
        JSONObject dto = new JSONObject();
        dto.put("password", password);
        String path = ApiPath.USER_PASSWORD_PREFIX + userId + ApiPath.USER_PASSWORD_SUFFIX;
        return new JSONObject(ApiClient.put(path, dto.toString()));
    }

    public Set<String> permissions() throws Exception {
        String res = ApiClient.get(ApiPath.USER_PERMISSIONS, Map.of());
        JSONArray array = parsePermissionArray(res);
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            String code = array.optString(i, "");
            if (!code.isBlank()) {
                codes.add(code);
            }
        }
        return codes;
    }

    private JSONArray parsePermissionArray(String response) {
        String body = response == null ? "" : response.trim();
        if (body.startsWith("[")) {
            return new JSONArray(body);
        }
        JSONObject json = new JSONObject(body);
        JSONArray data = json.optJSONArray("data");
        return data == null ? new JSONArray() : data;
    }
}
