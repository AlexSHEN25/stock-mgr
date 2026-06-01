package co.handk.client.service;

import co.handk.client.constant.AppConstants.ApiPath;
import co.handk.client.util.ApiClient;
import org.json.JSONObject;

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
}
