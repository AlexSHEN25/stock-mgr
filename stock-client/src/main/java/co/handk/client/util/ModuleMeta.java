package co.handk.client.util;

import java.util.*;

public final class ModuleMeta {

    private ModuleMeta() {
    }

    public enum FieldType {
        TEXT,
        NUMBER,
        SELECT,
        RELATION
    }

    private static final Map<String, List<String>> QUERY_FIELDS = new HashMap<>();
    private static final Map<String, Map<String, FieldType>> FIELD_TYPES = new HashMap<>();
    private static final Map<String, String> RELATION_FIELD_MODULE = new HashMap<>();
    private static final Map<String, String> NAME_TO_ID_FIELD = new HashMap<>();
    private static final Map<String, String> FIELD_LABELS = new HashMap<>();

    static {
        QUERY_FIELDS.put("user", List.of("username", "deptId", "email", "phone", "status"));
        QUERY_FIELDS.put("dept", List.of("id", "name", "code", "leaderId", "status"));
        QUERY_FIELDS.put("warehouse", List.of("id", "name", "code", "managerId", "status"));
        QUERY_FIELDS.put("role", List.of("id", "name", "code", "status"));
        QUERY_FIELDS.put("permission", List.of("id", "name", "code", "module", "type", "status"));
        QUERY_FIELDS.put("goods", List.of("id", "name", "englishName", "brandId", "seriesId", "categoryId", "makerId", "status", "isHot"));
        QUERY_FIELDS.put("goodsLevelPrice", List.of("id", "goodsId", "skuId", "levelId", "status"));
        QUERY_FIELDS.put("maker", List.of("id", "name", "status"));
        QUERY_FIELDS.put("brand", List.of("id", "name", "englishName", "status"));
        QUERY_FIELDS.put("category", List.of("id", "name", "status"));
        QUERY_FIELDS.put("series", List.of("id", "name", "englishName", "brandId", "status"));
        QUERY_FIELDS.put("stock", List.of("id", "goodsName", "skuCode", "skuId", "stockTypeId", "warehouseId", "status"));
        QUERY_FIELDS.put("stockType", List.of("id", "name", "status"));
        QUERY_FIELDS.put("stockRecord", List.of("id", "goodsName", "skuCode", "warehouseId"));
        QUERY_FIELDS.put("stockOrder", List.of("id", "orderNo", "warehouseId", "requesterId", "operatorId"));
        QUERY_FIELDS.put("stockOrderItem", List.of("id", "orderId", "goodsName", "skuCode"));
        QUERY_FIELDS.put("requestForm", List.of("id", "bizNo", "deptId", "customerId", "warehouseId", "approverId"));
        QUERY_FIELDS.put("requestItem", List.of("id", "requestId", "goodsName", "skuCode", "warehouseId"));
        QUERY_FIELDS.put("priceRecord", List.of("id", "goodsName", "skuCode", "operatorId"));
        QUERY_FIELDS.put("customer", List.of("id", "customerCode", "name", "ownerUserId", "ownerDeptId", "status"));
        QUERY_FIELDS.put("customerLevel", List.of("id", "name", "status"));
        QUERY_FIELDS.put("config", List.of("id", "name", "group", "title", "type"));
        QUERY_FIELDS.put("message", List.of("id", "type", "userId", "isRead", "state"));
        QUERY_FIELDS.put("operateLog", List.of("id", "userId", "module", "operation", "status"));

        RELATION_FIELD_MODULE.put("deptId", "dept");
        RELATION_FIELD_MODULE.put("managerId", "user");
        RELATION_FIELD_MODULE.put("leaderId", "user");
        RELATION_FIELD_MODULE.put("roleId", "role");
        RELATION_FIELD_MODULE.put("permissionId", "permission");
        RELATION_FIELD_MODULE.put("seriesId", "series");
        RELATION_FIELD_MODULE.put("brandId", "brand");
        RELATION_FIELD_MODULE.put("categoryId", "category");
        RELATION_FIELD_MODULE.put("makerId", "maker");
        RELATION_FIELD_MODULE.put("goodsId", "goods");
        RELATION_FIELD_MODULE.put("skuId", "goodsSku");
        RELATION_FIELD_MODULE.put("warehouseId", "warehouse");
        RELATION_FIELD_MODULE.put("requesterId", "user");
        RELATION_FIELD_MODULE.put("operatorId", "user");
        RELATION_FIELD_MODULE.put("approverId", "user");
        RELATION_FIELD_MODULE.put("ownerUserId", "user");
        RELATION_FIELD_MODULE.put("ownerDeptId", "dept");

        NAME_TO_ID_FIELD.put("deptName", "deptId");
        NAME_TO_ID_FIELD.put("roleName", "roleId");
        NAME_TO_ID_FIELD.put("permissionName", "permissionId");
        NAME_TO_ID_FIELD.put("managerName", "managerId");
        NAME_TO_ID_FIELD.put("leaderName", "leaderId");
        NAME_TO_ID_FIELD.put("seriesName", "seriesId");
        NAME_TO_ID_FIELD.put("brandName", "brandId");
        NAME_TO_ID_FIELD.put("categoryName", "categoryId");
        NAME_TO_ID_FIELD.put("makerName", "makerId");
        NAME_TO_ID_FIELD.put("goodsName", "goodsId");
        NAME_TO_ID_FIELD.put("skuName", "skuId");
        NAME_TO_ID_FIELD.put("warehouseName", "warehouseId");
        NAME_TO_ID_FIELD.put("requesterName", "requesterId");
        NAME_TO_ID_FIELD.put("operatorName", "operatorId");
        NAME_TO_ID_FIELD.put("approverName", "approverId");

        FIELD_LABELS.put("id", "ID");
        FIELD_LABELS.put("username", "用户名");
        FIELD_LABELS.put("password", "密码");
        FIELD_LABELS.put("deptId", "部门");
        FIELD_LABELS.put("deptName", "部门名称");
        FIELD_LABELS.put("roleId", "角色");
        FIELD_LABELS.put("permissionId", "权限");
        FIELD_LABELS.put("name", "名称");
        FIELD_LABELS.put("englishName", "英文名");
        FIELD_LABELS.put("code", "编码");
        FIELD_LABELS.put("email", "邮箱");
        FIELD_LABELS.put("phone", "电话");
        FIELD_LABELS.put("warehouseId", "仓库");
        FIELD_LABELS.put("goodsId", "商品");
        FIELD_LABELS.put("goodsName", "商品名称");
        FIELD_LABELS.put("skuId", "SKU");
        FIELD_LABELS.put("skuCode", "SKU编码");
        FIELD_LABELS.put("categoryId", "分类");
        FIELD_LABELS.put("brandId", "品牌");
        FIELD_LABELS.put("makerId", "制造商");
        FIELD_LABELS.put("seriesId", "系列");
        FIELD_LABELS.put("status", "状态");
        FIELD_LABELS.put("statusDesc", "状态");
        FIELD_LABELS.put("address", "地址");
        FIELD_LABELS.put("remark", "备注");
        FIELD_LABELS.put("createTime", "创建时间");
        FIELD_LABELS.put("updateTime", "更新时间");
        FIELD_LABELS.put("orderNo", "单据编号");
        FIELD_LABELS.put("bizNo", "业务编号");
        FIELD_LABELS.put("customerCode", "客户编码");
        FIELD_LABELS.put("isHot", "人气商品");

        setType("status", FieldType.SELECT);
        setType("isRead", FieldType.SELECT);
        setType("state", FieldType.SELECT);
        setType("isHot", FieldType.SELECT);

        RELATION_FIELD_MODULE.keySet().forEach(k -> setType(k, FieldType.RELATION));
    }

    private static void setType(String field, FieldType type) {
        FIELD_TYPES.computeIfAbsent("_GLOBAL", k -> new HashMap<>()).put(field, type);
    }

    public static List<String> queryFields(String moduleKey) {
        return QUERY_FIELDS.getOrDefault(moduleKey, List.of("name"));
    }

    public static FieldType fieldType(String moduleKey, String field) {
        FieldType global = FIELD_TYPES.getOrDefault("_GLOBAL", Map.of()).get(field);
        if (global != null) {
            return global;
        }
        String low = String.valueOf(field).toLowerCase(Locale.ROOT);
        if (low.endsWith("id")) {
            return FieldType.NUMBER;
        }
        if (low.contains("price") || low.contains("qty") || low.contains("count") || low.contains("sort")) {
            return FieldType.NUMBER;
        }
        return FieldType.TEXT;
    }

    public static String relationModuleByField(String field) {
        return RELATION_FIELD_MODULE.get(field);
    }

    public static String mapNameFieldToIdField(String field) {
        return NAME_TO_ID_FIELD.getOrDefault(field, "");
    }

    public static String normalizeTitle(String key) {
        if (FIELD_LABELS.containsKey(key)) {
            return FIELD_LABELS.get(key);
        }
        return key;
    }
}
