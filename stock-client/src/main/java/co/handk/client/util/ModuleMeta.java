package co.handk.client.util;

import static co.handk.client.constant.AppConstants.Field.ID;
import static co.handk.client.constant.AppConstants.Module.GOODS;
import static co.handk.client.constant.AppConstants.Module.GOODS_SKU;
import static co.handk.client.constant.AppConstants.Module.REQUEST_FORM;
import static co.handk.client.constant.AppConstants.Module.REQUEST_ITEM;
import static co.handk.client.constant.AppConstants.Module.SERIES;
import static co.handk.client.constant.AppConstants.Module.STOCK_ORDER;
import static co.handk.client.constant.AppConstants.Module.STOCK_ORDER_ITEM;
import static co.handk.client.constant.AppConstants.Module.USER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public final class ModuleMeta {

    private ModuleMeta() {
    }

    public enum FieldType {
        TEXT, NUMBER, SELECT, RELATION
    }

    public enum RowActionType {
        DETAIL, DOWNLOAD_REQUEST_FORM, PREVIEW_FIELDS, MARK_READ
    }

    public static final class Option {
        public final String label;
        public final String value;

        public Option(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    public static final class DependencyRule {
        public final String parentField;
        public final String childField;
        public final String sourceModule;
        public final String queryParam;
        public final List<String> cascadeClearFields;

        public DependencyRule(String parentField, String childField, String sourceModule, String queryParam, List<String> cascadeClearFields) {
            this.parentField = parentField;
            this.childField = childField;
            this.sourceModule = sourceModule;
            this.queryParam = queryParam;
            this.cascadeClearFields = cascadeClearFields;
        }
    }

    public static final class RowAction {
        public final RowActionType type;
        public final String titleKey;
        public final String targetModule;
        public final String filterField;
        public final List<String> detailFields;

        public RowAction(RowActionType type, String titleKey, String targetModule, String filterField) {
            this(type, titleKey, targetModule, filterField, List.of());
        }

        public RowAction(RowActionType type, String titleKey, String targetModule, String filterField, List<String> detailFields) {
            this.type = type;
            this.titleKey = titleKey;
            this.targetModule = targetModule;
            this.filterField = filterField;
            this.detailFields = detailFields;
        }
    }

    public static final class ModuleActionPolicy {
        public final boolean canCreate;
        public final boolean canInlineEdit;
        public final boolean canEdit;
        public final boolean canBatchDelete;
        public final boolean canDelete;

        public ModuleActionPolicy(boolean canCreate, boolean canInlineEdit, boolean canEdit, boolean canBatchDelete, boolean canDelete) {
            this.canCreate = canCreate;
            this.canInlineEdit = canInlineEdit;
            this.canEdit = canEdit;
            this.canBatchDelete = canBatchDelete;
            this.canDelete = canDelete;
        }
    }

    private static final String MODULE_DEPT = "dept";
    private static final String MODULE_WAREHOUSE = "warehouse";
    private static final String MODULE_ROLE = "role";
    private static final String MODULE_PERMISSION = "permission";
    private static final String MODULE_GOODS_LEVEL_PRICE = "goodsLevelPrice";
    private static final String MODULE_MAKER = "maker";
    private static final String MODULE_BRAND = "brand";
    private static final String MODULE_CATEGORY = "category";
    private static final String MODULE_STOCK = "stock";
    private static final String MODULE_STOCK_TYPE = "stockType";
    private static final String MODULE_STOCK_RECORD = "stockRecord";
    private static final String MODULE_PRICE_RECORD = "priceRecord";
    private static final String MODULE_CUSTOMER = "customer";
    private static final String MODULE_CUSTOMER_LEVEL = "customerLevel";
    private static final String MODULE_CONFIG = "config";
    private static final String MODULE_MESSAGE = "message";
    private static final String MODULE_OPERATE_LOG = "operateLog";
    private static final String MODULE_GOODS_SKU_SPEC = "goodsSkuSpec";
    private static final String MODULE_GOODS_IMAGE = "goodsImage";
    private static final String MODULE_USER_ROLE = "userRole";
    private static final String MODULE_ROLE_PERMISSION = "rolePermission";
    private static final String MODULE_USER_TOKEN = "userToken";
    private static final String DEFAULT_FIELD_TITLE = "項目";

    private static final Map<String, List<String>> QUERY_FIELDS = new HashMap<>();
    private static final Map<String, List<String>> FORM_FIELDS = new HashMap<>();
    private static final Map<String, List<String>> REQUIRED_FORM_FIELDS = new HashMap<>();
    private static final Map<String, Map<String, FieldType>> FIELD_TYPES = new HashMap<>();
    private static final Map<String, String> RELATION_FIELD_MODULE = new HashMap<>();
    private static final Map<String, String> NAME_TO_ID_FIELD = new HashMap<>();
    private static final Map<String, List<Option>> SELECT_OPTIONS = new HashMap<>();
    private static final Map<String, List<DependencyRule>> DEPENDENCY_RULES = new HashMap<>();
    private static final Map<String, List<RowAction>> ROW_ACTIONS = new HashMap<>();
    private static final Map<String, List<String>> HIDDEN_LIST_FIELDS = new HashMap<>();
    private static final Map<String, ModuleActionPolicy> ACTION_POLICIES = new HashMap<>();
    private static final List<String> INLINE_READONLY_FIELDS = List.of(
            "id", "createTime", "updateTime", "statusDesc", "beforeQty", "afterQty"
    );
    private static final ResourceBundle UI_BUNDLE = ResourceBundle.getBundle("i18n.ui", Locale.JAPAN);

    static {
        QUERY_FIELDS.put(USER, List.of("username", "deptId", "deptName", "roleId", "roleName", "email", "phone", "status"));
        QUERY_FIELDS.put(MODULE_DEPT, List.of(ID, "name", "code", "leaderId", "sort", "status"));
        QUERY_FIELDS.put(MODULE_WAREHOUSE, List.of(ID, "name", "code", "address", "managerId", "status"));
        QUERY_FIELDS.put(MODULE_ROLE, List.of(ID, "name", "code", "permissionNames", "remark", "status"));
        QUERY_FIELDS.put(MODULE_PERMISSION, List.of(ID, "name", "code", "module", "type", "parentId", "path", "sort", "icon", "component", "status"));
        QUERY_FIELDS.put(GOODS, List.of(ID, "name", "englishName", "seriesId", "brandId", "categoryId", "makerId", "sort", "status", "isHot"));
        QUERY_FIELDS.put(MODULE_GOODS_LEVEL_PRICE, List.of(ID, "goodsId", "skuId", "skuCode", "levelId", "price", "currency", "discount", "effectiveTime", "expireTime", "status"));
        QUERY_FIELDS.put(MODULE_MAKER, List.of(ID, "name", "status"));
        QUERY_FIELDS.put(MODULE_BRAND, List.of(ID, "name", "englishName", "image", "content", "status"));
        QUERY_FIELDS.put(MODULE_CATEGORY, List.of(ID, "name", "status"));
        QUERY_FIELDS.put(SERIES, List.of(ID, "name", "englishName", "brandId", "status"));
        QUERY_FIELDS.put(MODULE_STOCK, List.of(ID, "goodsId", "goodsName", "skuCode", "skuId", "stockTypeId", "currentQty", "lockQty", "price", "priceUpdateTime", "currency", "warehouseId", "status"));
        QUERY_FIELDS.put(MODULE_STOCK_TYPE, List.of(ID, "name", "status"));
        QUERY_FIELDS.put(STOCK_ORDER, List.of(ID, "orderNo", "orderType", "bizDate", "stockTypeId", "warehouseId", "sourceType", "sourceId", "totalQty", "state", "requesterId", "requesterName", "operatorId", "operatorName", "approverId", "approverName", "approveTime", "finishTime", "remark"));
        QUERY_FIELDS.put(STOCK_ORDER_ITEM, List.of(ID, "orderId", "goodsId", "skuId", "skuCode", "goodsName", "englishName", "brandId", "brandName", "seriesId", "seriesName", "categoryId", "categoryName", "stockTypeId", "stockTypeName", "makerId", "makerName", "changeQty", "price", "currency", "remark"));
        QUERY_FIELDS.put(REQUEST_FORM, List.of(ID, "bizNo", "sourceOrderId", "sourceOrderNo", "userId", "username", "deptId", "deptName", "customerId", "customerName", "warehouseId", "totalQty", "requestQty", "totalAmt", "state", "approverId", "approverName", "approveTime", "approveRemark"));
        QUERY_FIELDS.put(REQUEST_ITEM, List.of(ID, "requestId", "goodsId", "skuId", "skuCode", "goodsName", "englishName", "brandId", "brandName", "seriesId", "seriesName", "categoryId", "categoryName", "makerId", "makerName", "stockTypeId", "stockTypeName", "warehouseId", "price", "currency", "discount", "requestQty", "approveQty", "outQty", "stockRecordId", "remark"));
        QUERY_FIELDS.put(MODULE_STOCK_RECORD, List.of(ID, "bizNo", "orderId", "orderItemId", "stockId", "goodsId", "skuId", "skuCode", "goodsName", "englishName", "brandId", "brandName", "seriesId", "seriesName", "categoryId", "categoryName", "stockTypeId", "stockTypeName", "makerId", "makerName", "warehouseId", "changeQty", "sourceType", "orderType", "bizDate", "price", "currency", "priceUpdateTime", "customerId", "customerName", "requesterId", "requesterName", "operatorId", "operatorName", "remark"));
        QUERY_FIELDS.put(MODULE_PRICE_RECORD, List.of(ID, "goodsId", "goodsName", "englishName", "skuId", "skuCode", "oldPrice", "newPrice", "currency", "discount", "priceUpdateTime", "operatorId", "operatorName"));
        QUERY_FIELDS.put(MODULE_CUSTOMER, List.of(ID, "customerCode", "name", "englishName", "contactPerson", "phone", "email", "country", "city", "address", "levelName", "ownerUserName", "ownerDeptName", "remark", "status"));
        QUERY_FIELDS.put(MODULE_CUSTOMER_LEVEL, List.of(ID, "name", "discount", "remark", "status"));
        QUERY_FIELDS.put(MODULE_CONFIG, List.of(ID, "name", "group", "title", "tip", "type", "value", "content"));
        QUERY_FIELDS.put(MODULE_MESSAGE, List.of(ID, "type", "userId", "message", "sourceId", "state"));
        QUERY_FIELDS.put(MODULE_OPERATE_LOG, List.of(ID, "userId", "username", "module", "operation", "method", "requestUrl", "requestIp", "requestParam", "responseData", "status", "errorMsg", "costTime"));
        QUERY_FIELDS.put(GOODS_SKU, List.of(ID, "goodsId", "skuCode", "skuName", "price", "currency", "costPrice", "updatePrice", "priceUpdateTime", "barcode", "weight", "volume", "status"));
        QUERY_FIELDS.put(MODULE_GOODS_SKU_SPEC, List.of(ID, "skuId", "skuCode", "specId", "specName", "specValue", "sort"));
        QUERY_FIELDS.put(MODULE_GOODS_IMAGE, List.of(ID, "goodsId", "skuId", "skuCode", "imageUrl", "sort"));
        QUERY_FIELDS.put(MODULE_USER_ROLE, List.of(ID, "userId", "roleId"));
        QUERY_FIELDS.put(MODULE_ROLE_PERMISSION, List.of(ID, "roleId", "permissionId"));
        QUERY_FIELDS.put(MODULE_USER_TOKEN, List.of(ID, "token", "userId", "loginTime", "expireTime", "loginIp", "status"));

        FORM_FIELDS.put(USER, List.of("username", "password", "deptId", "roleId", "email", "phone", "avatar", "status"));
        FORM_FIELDS.put(MODULE_DEPT, List.of("parentId", "name", "code", "leaderId", "sort", "status"));
        FORM_FIELDS.put(GOODS, List.of("name", "englishName", "brandId", "seriesId", "categoryId", "makerId", "description", "isHot", "skuCode", "skuName", "price", "status"));
        FORM_FIELDS.put(MODULE_STOCK, List.of("goodsId", "sourceType", "warehouseId", "stockTypeId", "quantity", "remark", "status"));
        FORM_FIELDS.put(STOCK_ORDER, List.of("orderNo", "orderType", "bizDate", "warehouseId", "sourceType", "sourceId", "totalQty", "stockTypeId", "state", "requesterId", "operatorId", "approverId", "approveTime", "finishTime", "remark"));
        FORM_FIELDS.put(STOCK_ORDER_ITEM, List.of("orderId", "goodsId", "skuId", "skuCode", "goodsName", "englishName", "brandId", "brandName", "seriesId", "seriesName", "categoryId", "categoryName", "stockTypeId", "stockTypeName", "makerId", "makerName", "changeQty", "price", "currency", "remark"));
        FORM_FIELDS.put(REQUEST_FORM, List.of("bizNo", "sourceOrderId", "sourceOrderNo", "userId", "username", "deptId", "deptName", "customerId", "customerName", "warehouseId", "totalQty", "requestQty", "totalAmt", "state", "approverId", "approverName", "approveTime", "approveRemark"));
        FORM_FIELDS.put(REQUEST_ITEM, List.of("requestId", "goodsId", "skuId", "skuCode", "goodsName", "englishName", "brandId", "brandName", "seriesId", "seriesName", "categoryId", "categoryName", "makerId", "makerName", "stockTypeId", "stockTypeName", "warehouseId", "price", "currency", "discount", "requestQty", "approveQty", "outQty", "stockRecordId", "remark"));
        FORM_FIELDS.put(MODULE_WAREHOUSE, List.of("name", "code", "address", "managerId", "status"));
        FORM_FIELDS.put(MODULE_STOCK_TYPE, List.of("name", "status"));
        FORM_FIELDS.put(MODULE_ROLE, List.of("name", "code", "permissionIds", "remark", "status"));
        FORM_FIELDS.put(MODULE_MAKER, List.of("name", "status"));
        FORM_FIELDS.put(MODULE_BRAND, List.of("name", "englishName", "image", "content", "status"));
        FORM_FIELDS.put(MODULE_CATEGORY, List.of("name", "status"));
        FORM_FIELDS.put(SERIES, List.of("name", "englishName", "brandId", "content", "status"));
        FORM_FIELDS.put(MODULE_CUSTOMER, List.of("customerCode", "name", "englishName", "contactPerson", "phone", "email", "country", "city", "address", "levelId", "ownerUserId", "ownerDeptId", "remark", "status"));

        REQUIRED_FORM_FIELDS.put(USER, List.of("username", "password", "deptId", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_DEPT, List.of("name", "code", "status"));
        REQUIRED_FORM_FIELDS.put(GOODS, List.of("name", "englishName", "brandId", "seriesId", "categoryId", "makerId", "skuCode"));
        REQUIRED_FORM_FIELDS.put(MODULE_STOCK, List.of("goodsId", "sourceType", "warehouseId", "stockTypeId", "quantity"));
        REQUIRED_FORM_FIELDS.put(STOCK_ORDER, List.of("orderNo", "orderType", "warehouseId", "sourceType"));
        REQUIRED_FORM_FIELDS.put(STOCK_ORDER_ITEM, List.of("orderId", "goodsId", "skuId", "goodsName", "changeQty"));
        REQUIRED_FORM_FIELDS.put(REQUEST_FORM, List.of("bizNo", "sourceOrderId", "userId", "username", "customerId", "customerName"));
        REQUIRED_FORM_FIELDS.put(REQUEST_ITEM, List.of("requestId", "goodsId", "skuId"));
        REQUIRED_FORM_FIELDS.put(MODULE_WAREHOUSE, List.of("name", "code", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_ROLE, List.of("name", "code", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_MAKER, List.of("name", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_BRAND, List.of("name", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_CATEGORY, List.of("name", "status"));
        REQUIRED_FORM_FIELDS.put(SERIES, List.of("name", "brandId", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_CUSTOMER, List.of("customerCode", "name", "status"));

        RELATION_FIELD_MODULE.put("deptId", MODULE_DEPT);
        RELATION_FIELD_MODULE.put("managerId", USER);
        RELATION_FIELD_MODULE.put("leaderId", USER);
        RELATION_FIELD_MODULE.put("roleId", MODULE_ROLE);
        RELATION_FIELD_MODULE.put("permissionId", MODULE_PERMISSION);
        RELATION_FIELD_MODULE.put("permissionIds", MODULE_PERMISSION);
        RELATION_FIELD_MODULE.put("parentId", MODULE_PERMISSION);
        RELATION_FIELD_MODULE.put("sourceOrderId", STOCK_ORDER);
        RELATION_FIELD_MODULE.put("seriesId", SERIES);
        RELATION_FIELD_MODULE.put("brandId", MODULE_BRAND);
        RELATION_FIELD_MODULE.put("categoryId", MODULE_CATEGORY);
        RELATION_FIELD_MODULE.put("makerId", MODULE_MAKER);
        RELATION_FIELD_MODULE.put("goodsId", GOODS);
        RELATION_FIELD_MODULE.put("skuId", GOODS_SKU);
        RELATION_FIELD_MODULE.put("stockTypeId", MODULE_STOCK_TYPE);
        RELATION_FIELD_MODULE.put("warehouseId", MODULE_WAREHOUSE);
        RELATION_FIELD_MODULE.put("customerId", MODULE_CUSTOMER);
        RELATION_FIELD_MODULE.put("levelId", MODULE_CUSTOMER_LEVEL);
        RELATION_FIELD_MODULE.put("ownerUserId", USER);
        RELATION_FIELD_MODULE.put("ownerDeptId", MODULE_DEPT);
        RELATION_FIELD_MODULE.put("userId", USER);
        RELATION_FIELD_MODULE.put("requesterId", USER);
        RELATION_FIELD_MODULE.put("operatorId", USER);
        RELATION_FIELD_MODULE.put("approverId", USER);

        NAME_TO_ID_FIELD.put("deptName", "deptId");
        NAME_TO_ID_FIELD.put("roleName", "roleId");
        NAME_TO_ID_FIELD.put("permissionName", "permissionId");
        NAME_TO_ID_FIELD.put("permissionNames", "permissionIds");
        NAME_TO_ID_FIELD.put("managerName", "managerId");
        NAME_TO_ID_FIELD.put("leaderName", "leaderId");
        NAME_TO_ID_FIELD.put("seriesName", "seriesId");
        NAME_TO_ID_FIELD.put("brandName", "brandId");
        NAME_TO_ID_FIELD.put("categoryName", "categoryId");
        NAME_TO_ID_FIELD.put("makerName", "makerId");
        NAME_TO_ID_FIELD.put("goodsName", "goodsId");
        NAME_TO_ID_FIELD.put("skuName", "skuId");
        NAME_TO_ID_FIELD.put("stockTypeName", "stockTypeId");
        NAME_TO_ID_FIELD.put("warehouseName", "warehouseId");
        NAME_TO_ID_FIELD.put("sourceOrderNo", "sourceOrderId");
        NAME_TO_ID_FIELD.put("customerName", "customerId");
        NAME_TO_ID_FIELD.put("levelName", "levelId");
        NAME_TO_ID_FIELD.put("ownerUserName", "ownerUserId");
        NAME_TO_ID_FIELD.put("ownerDeptName", "ownerDeptId");

        setType("status", FieldType.SELECT);
        setType("state", FieldType.SELECT);
        setType("sourceType", FieldType.SELECT);
        setType("orderType", FieldType.SELECT);
        setType("isRead", FieldType.SELECT);
        setType("isHot", FieldType.SELECT);
        RELATION_FIELD_MODULE.keySet().forEach(key -> setType(key, FieldType.RELATION));

        putOptions("status", List.of(new Option("有効", "1"), new Option("無効", "0")));
        putOptions(STOCK_ORDER + ".orderType", List.of(
                new Option("入庫", "1"), new Option("出庫", "2"), new Option("調整", "3"),
                new Option("棚卸", "4"), new Option("移動", "5"), new Option("返品", "6")
        ));
        putOptions(STOCK_ORDER + ".sourceType", List.of(
                new Option("注文", "1"), new Option("返品", "2"), new Option("申請書", "3"), new Option("手動", "4")
        ));
        putOptions(STOCK_ORDER + ".state", List.of(
                new Option("草稿", "0"), new Option("審査中", "1"), new Option("完了", "2"), new Option("取消", "3")
        ));
        putOptions(REQUEST_FORM + ".state", List.of(
                new Option("草稿", "0"), new Option("審査中", "1"), new Option("完了", "2"), new Option("取消", "3")
        ));
        putOptions(MODULE_STOCK + ".sourceType", List.of(
                new Option("自社入庫（承認必須）", "1"), new Option("再販売入庫（即時入庫）", "2")
        ));
        putOptions(MODULE_MESSAGE + ".isRead", List.of(
                new Option("未読", "0"), new Option("既読", "1")
        ));

        DEPENDENCY_RULES.put(GOODS, List.of(
                new DependencyRule("brandId", "seriesId", SERIES, "brandId", List.of("goodsId", "skuId")),
                new DependencyRule("seriesId", "goodsId", GOODS, "seriesId", List.of("skuId")),
                new DependencyRule("categoryId", "goodsId", GOODS, "categoryId", List.of("skuId")),
                new DependencyRule("goodsId", "skuId", GOODS_SKU, "goodsId", List.of())
        ));
        DEPENDENCY_RULES.put(MODULE_STOCK, List.of(new DependencyRule("goodsId", "skuId", GOODS_SKU, "goodsId", List.of())));
        DEPENDENCY_RULES.put(REQUEST_ITEM, List.of(new DependencyRule("goodsId", "skuId", GOODS_SKU, "goodsId", List.of())));
        DEPENDENCY_RULES.put(STOCK_ORDER_ITEM, List.of(new DependencyRule("goodsId", "skuId", GOODS_SKU, "goodsId", List.of())));

        ROW_ACTIONS.put(STOCK_ORDER, List.of(
                new RowAction(RowActionType.DETAIL, "action.orderDetail", STOCK_ORDER_ITEM, "orderId")
        ));
        ROW_ACTIONS.put(REQUEST_FORM, List.of(
                new RowAction(RowActionType.DETAIL, "action.requestDetail", REQUEST_ITEM, "requestId"),
                new RowAction(RowActionType.DOWNLOAD_REQUEST_FORM, "action.download", null, null)
        ));
        ROW_ACTIONS.put(GOODS_SKU, List.of(
                new RowAction(RowActionType.PREVIEW_FIELDS, "action.detail", null, null,
                        List.of("costPrice", "updatePrice", "priceUpdateTime", "barcode", "weight", "volume"))
        ));
        ROW_ACTIONS.put(MODULE_GOODS_IMAGE, List.of(
                new RowAction(RowActionType.PREVIEW_FIELDS, "action.detail", null, null, List.of("imageUrl"))
        ));
        ROW_ACTIONS.put(MODULE_MESSAGE, List.of(
                new RowAction(RowActionType.MARK_READ, "action.read", null, null)
        ));

        HIDDEN_LIST_FIELDS.put(GOODS_SKU, List.of("costPrice", "updatePrice", "priceUpdateTime", "barcode", "weight", "volume"));
        HIDDEN_LIST_FIELDS.put(MODULE_GOODS_IMAGE, List.of("imageUrl"));

        ACTION_POLICIES.put(MODULE_STOCK_RECORD, new ModuleActionPolicy(false, false, false, false, false));
        ACTION_POLICIES.put(MODULE_PRICE_RECORD, new ModuleActionPolicy(false, false, false, false, false));
        ACTION_POLICIES.put(MODULE_OPERATE_LOG, new ModuleActionPolicy(false, false, false, false, false));
        ACTION_POLICIES.put(MODULE_MESSAGE, new ModuleActionPolicy(false, false, false, false, false));
        ACTION_POLICIES.put(GOODS, new ModuleActionPolicy(true, false, true, true, true));
    }

    private static void setType(String field, FieldType type) {
        FIELD_TYPES.computeIfAbsent("_GLOBAL", key -> new HashMap<>()).put(field, type);
    }

    private static void putOptions(String key, List<Option> options) {
        SELECT_OPTIONS.put(key, options);
    }

    public static List<String> queryFields(String moduleKey) {
        return QUERY_FIELDS.getOrDefault(moduleKey, List.of("name"));
    }

    public static List<String> formFields(String moduleKey) {
        return FORM_FIELDS.getOrDefault(moduleKey, List.of());
    }

    public static boolean isRequiredFormField(String moduleKey, String field) {
        return REQUIRED_FORM_FIELDS.getOrDefault(moduleKey, List.of()).contains(field);
    }

    public static List<String> orderedColumns(String moduleKey, Iterable<String> keys) {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        ordered.add(ID);
        ordered.addAll(queryFields(moduleKey));
        for (String key : keys) {
            ordered.add(key);
        }
        ordered.remove("__selected");
        ordered.removeAll(HIDDEN_LIST_FIELDS.getOrDefault(moduleKey, List.of()));
        return new ArrayList<>(ordered);
    }

    public static FieldType fieldType(String moduleKey, String field) {
        FieldType global = FIELD_TYPES.getOrDefault("_GLOBAL", Map.of()).get(field);
        if (global != null) {
            return global;
        }
        String low = String.valueOf(field).toLowerCase(Locale.ROOT);
        if (low.endsWith("id") || low.contains("price") || low.contains("qty") || low.contains("count")
                || low.contains("sort") || low.contains("amount") || low.contains("discount")) {
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

    public static List<Option> selectOptions(String moduleKey, String field) {
        List<Option> moduleOptions = SELECT_OPTIONS.get(moduleKey + "." + field);
        if (moduleOptions != null) {
            return moduleOptions;
        }
        return SELECT_OPTIONS.getOrDefault(field, List.of());
    }

    public static Option optionByValue(String moduleKey, String field, String value) {
        for (Option option : selectOptions(moduleKey, field)) {
            if (option.value.equals(value)) {
                return option;
            }
        }
        return null;
    }

    public static Option optionByLabel(String moduleKey, String field, String label) {
        for (Option option : selectOptions(moduleKey, field)) {
            if (option.label.equals(label)) {
                return option;
            }
        }
        return null;
    }

    public static String normalizeTitle(String key) {
        String bundleKey = "field." + key;
        if (UI_BUNDLE.containsKey(bundleKey)) {
            String value = UI_BUNDLE.getString(bundleKey);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return DEFAULT_FIELD_TITLE;
    }

    public static List<DependencyRule> dependencyRules(String moduleKey) {
        return DEPENDENCY_RULES.getOrDefault(moduleKey, List.of());
    }

    public static List<RowAction> rowActions(String moduleKey) {
        return ROW_ACTIONS.getOrDefault(moduleKey, List.of());
    }

    public static ModuleActionPolicy actionPolicy(String moduleKey) {
        return ACTION_POLICIES.getOrDefault(moduleKey, new ModuleActionPolicy(true, true, true, true, true));
    }

    public static boolean isInlineReadonlyField(String field) {
        if (field == null || field.isBlank()) {
            return true;
        }
        String f = field.trim();
        for (String readonly : INLINE_READONLY_FIELDS) {
            if (readonly.equalsIgnoreCase(f)) {
                return true;
            }
        }
        return false;
    }
}
