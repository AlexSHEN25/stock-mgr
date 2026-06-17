package co.handk.client.service;

import co.handk.client.constant.UiText;
import co.handk.client.constant.AppConstants.ApiPath;
import co.handk.client.constant.ModuleEndpointStrategy;
import co.handk.client.util.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModuleDataService {

    public JSONObject fetchPage(String module, int pageNum, int pageSize, Map<String, String> filters) throws Exception {
        String res;
        if (ModuleEndpointStrategy.pageUsesPost(module)) {
            JSONObject body = new JSONObject();
            body.put("pageNum", pageNum);
            body.put("pageSize", pageSize);
            if (filters != null) {
                filters.forEach(body::put);
            }
            res = ApiClient.post(ModuleEndpointStrategy.pagePath(module), body.toString());
        } else {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("pageNum", String.valueOf(pageNum));
            params.put("pageSize", String.valueOf(pageSize));
            if (filters != null) {
                params.putAll(filters);
            }
            res = ApiClient.get(ModuleEndpointStrategy.pagePath(module), params);
        }
        return new JSONObject(res);
    }

    public JSONObject save(String module, boolean editMode, JSONObject dto) throws Exception {
        String res;
        if (editMode) {
            String id = String.valueOf(dto.get("id"));
            res = ApiClient.put(ModuleEndpointStrategy.updatePath(module, id), dto.toString());
        } else {
            res = ApiClient.post(ModuleEndpointStrategy.createPath(module), dto.toString());
        }
        return new JSONObject(res);
    }

    public JSONObject inboundStock(JSONObject dto) throws Exception {
        return new JSONObject(ApiClient.post(ApiPath.STOCK_INBOUND, dto.toString()));
    }

    public JSONObject outboundStock(JSONObject dto) throws Exception {
        return new JSONObject(ApiClient.post(ApiPath.STOCK_OUTBOUND, dto.toString()));
    }

    public byte[] downloadGoodsTemplate() throws Exception {
        return ApiClient.getBytes(ApiPath.GOODS_IMPORT_TEMPLATE);
    }

    public JSONObject importGoods(java.io.File file) throws Exception {
        return new JSONObject(ApiClient.postMultipart(ApiPath.GOODS_IMPORT_UPSERT, "file", file));
    }

    public JSONObject delete(String module, String id) throws Exception {
        String res = ApiClient.delete(ModuleEndpointStrategy.deletePath(module, id));
        return new JSONObject(res);
    }

    public List<Map<String, String>> fetchSimpleRelationOptions(String relationModule, Map<String, String> extraFilters) throws Exception {
        JSONObject page = fetchPage(relationModule, 1, 200, extraFilters == null ? Map.of() : extraFilters);
        JSONObject data = page.optJSONObject("data");
        if (data == null) {
            return List.of();
        }
        JSONArray records = data.optJSONArray("records");
        if (records == null) {
            return List.of();
        }
        List<Map<String, String>> options = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            JSONObject row = records.getJSONObject(i);
            String id = String.valueOf(row.opt("id"));
            String label = row.optString("orderNo",
                    row.optString("name",
                    row.optString("goodsName",
                            row.optString("skuName",
                                    row.optString("username",
                                            row.optString("code", String.format(UiText.RELATION_FALLBACK_PATTERN, id)))))));
            options.add(Map.of("label", label, "value", id));
        }
        return options;
    }
}
