package co.handk.client.service;

import co.handk.client.constant.AppConstants.ApiPath;
import co.handk.client.constant.ModuleEndpointStrategy;
import co.handk.client.util.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TableActionService {

    private static final Logger LOGGER = Logger.getLogger(TableActionService.class.getName());

    public int batchDelete(String module, List<String> ids) {
        List<String> normalizedIds = ids.stream()
                .filter(id -> id != null && !id.isBlank())
                .toList();
        String batchDeletePath = ModuleEndpointStrategy.batchDeletePath(module);
        if (batchDeletePath != null) {
            return batchDelete(module, normalizedIds, batchDeletePath);
        }
        int ok = 0;
        for (String id : normalizedIds) {
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

    private int batchDelete(String module, List<String> ids, String batchDeletePath) {
        if (ids.isEmpty()) {
            return 0;
        }
        try {
            JSONArray body = new JSONArray();
            ids.forEach(id -> body.put(Long.parseLong(id)));
            String res = ApiClient.delete(batchDeletePath, body.toString());
            JSONObject json = new JSONObject(res);
            int code = json.optInt("code", -1);
            return code == 200 || code == 0 ? ids.size() : 0;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Batch delete failed. module=" + module + ", ids=" + ids, ex);
            return 0;
        }
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

    public JSONObject readAllMessages() throws Exception {
        String res = ApiClient.put(ApiPath.MESSAGE_READ_ALL, "{}");
        return new JSONObject(res);
    }

    public JSONArray fetchRequestCandidateItems(String requestId) throws Exception {
        String path = ApiPath.REQUEST_FORM_DOWNLOAD_V2_PREFIX + requestId + ApiPath.REQUEST_FORM_CANDIDATE_ITEMS_SUFFIX;
        JSONObject json = new JSONObject(ApiClient.get(path, null));
        JSONArray data = json.optJSONArray("data");
        return data == null ? new JSONArray() : data;
    }

    public JSONObject matchRequestItems(String requestId, JSONArray items) throws Exception {
        JSONObject body = new JSONObject();
        body.put("requestId", Long.parseLong(requestId));
        body.put("items", items);
        return new JSONObject(ApiClient.post(ApiPath.REQUEST_FORM_MATCH_ITEMS, body.toString()));
    }

    public JSONObject addDeliveryScheduleItemsToRequest(String requestId, JSONArray items) throws Exception {
        JSONObject body = new JSONObject();
        body.put("requestId", Long.parseLong(requestId));
        body.put("items", items);
        return new JSONObject(ApiClient.post(ApiPath.DELIVERY_SCHEDULE_ADD_TO_REQUEST, body.toString()));
    }

    public JSONObject returnRequestItemsToDeliverySchedule(String requestId, JSONArray items) throws Exception {
        JSONObject body = new JSONObject();
        body.put("requestId", Long.parseLong(requestId));
        body.put("items", items);
        return new JSONObject(ApiClient.post(ApiPath.DELIVERY_SCHEDULE_RETURN_FROM_REQUEST, body.toString()));
    }

    public JSONObject approveStockOrder(String orderId, boolean approved, String remark) throws Exception {
        String path = "/stock/approve/" + orderId + "?approved=" + approved;
        if (remark != null && !remark.isBlank()) {
            path += "&remark=" + java.net.URLEncoder.encode(remark, java.nio.charset.StandardCharsets.UTF_8);
        }
        String res = ApiClient.post(path, "{}");
        return new JSONObject(res);
    }
}
