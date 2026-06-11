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
        String res = ApiClient.get(ApiPath.USER_PERMISSION_SCOPE, Map.of());
        Set<String> codes = new HashSet<>();
        JSONObject scope = new JSONObject(res);
        JSONArray menuCodes = scope.optJSONArray("menuCodes");
        if (menuCodes != null) {
            for (int i = 0; i < menuCodes.length(); i++) {
                String code = menuCodes.optString(i, "");
                if (!code.isBlank()) {
                    codes.add(code);
                }
            }
        }
        JSONArray permissionCodes = scope.optJSONArray("permissionCodes");
        if (permissionCodes != null) {
            for (int i = 0; i < permissionCodes.length(); i++) {
                String code = permissionCodes.optString(i, "");
                if (!code.isBlank()) {
                    codes.add(code);
                }
            }
        }
        JSONArray roleCodes = scope.optJSONArray("roleCodes");
        if (roleCodes != null) {
            for (int i = 0; i < roleCodes.length(); i++) {
                String code = roleCodes.optString(i, "");
                if (!code.isBlank()) {
                    codes.add(code);
                }
            }
        }
        return codes;
    }
}
