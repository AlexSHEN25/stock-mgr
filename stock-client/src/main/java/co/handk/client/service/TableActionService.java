package co.handk.client.service;

import co.handk.client.constant.AppConstants.ApiPath;
import co.handk.client.constant.ModuleEndpointStrategy;
import co.handk.client.util.ApiClient;
import org.json.JSONObject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TableActionService {

    private static final Logger LOGGER = Logger.getLogger(TableActionService.class.getName());

    public int batchDelete(String module, List<String> ids) {
        int ok = 0;
        for (String id : ids) {
            if (id == null || id.isBlank()) {
                continue;
            }
            try {
                String res = ApiClient.delete(ModuleEndpointStrategy.deletePath(module, id));
                JSONObject json = new JSONObject(res);
                int code = json.optInt("code", -1);
                if (code == 200 || code == 0) {
                    ok++;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Batch delete failed. module=" + module + ", id=" + id, ex);
            }
        }
        return ok;
    }

    public JSONObject deleteOne(String module, String id) throws Exception {
        String res = ApiClient.delete(ModuleEndpointStrategy.deletePath(module, id));
        return new JSONObject(res);
    }

    public byte[] downloadRequestForm(String id) throws Exception {
        return downloadRequestForm(id, "excel");
    }

    public byte[] downloadRequestForm(String id, String format) throws Exception {
        if ("pdf".equalsIgnoreCase(format)) {
            return ApiClient.getBytes(ApiPath.REQUEST_FORM_DOWNLOAD_V2_PREFIX + id + ApiPath.REQUEST_FORM_PDF_V2_SUFFIX);
        }
        try {
            return ApiClient.getBytes(ApiPath.REQUEST_FORM_DOWNLOAD_V1 + id + "?format=excel");
        } catch (Exception first) {
            return ApiClient.getBytes(ApiPath.REQUEST_FORM_DOWNLOAD_V2_PREFIX + id + ApiPath.REQUEST_FORM_DOWNLOAD_V2_SUFFIX);
        }
    }

    public JSONObject readMessage(String id) throws Exception {
        String res = ApiClient.put(ApiPath.MESSAGE_READ_PREFIX + id, "{}");
        return new JSONObject(res);
    }
}
