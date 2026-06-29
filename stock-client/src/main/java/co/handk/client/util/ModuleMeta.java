package co.handk.client.util;

import co.handk.client.model.Session;

import static co.handk.client.constant.AppConstants.Field.ID;
import static co.handk.client.constant.AppConstants.Module.GOODS;
import static co.handk.client.constant.AppConstants.Module.GOODS_SKU;
import static co.handk.client.constant.AppConstants.Module.DELIVERY_SCHEDULE;
import static co.handk.client.constant.AppConstants.Module.BRAND_HIERARCHY;
import static co.handk.client.constant.AppConstants.Module.REQUEST_FORM;
import static co.handk.client.constant.AppConstants.Module.REQUEST_ITEM;
import static co.handk.client.constant.AppConstants.Module.SERIES;
import static co.handk.client.constant.AppConstants.Module.STOCK_ORDER;
import static co.handk.client.constant.AppConstants.Module.STOCK_ORDER_ITEM;
import static co.handk.client.constant.AppConstants.Module.USER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.json.JSONObject;

public final class ModuleMeta {

    private ModuleMeta() {
    }

    public enum FieldType {
        TEXT, NUMBER, SELECT, RELATION
    }

    public enum RowActionType {
        DETAIL, DOWNLOAD_REQUEST_FORM, MATCH_REQUEST_ITEMS, PREVIEW_FIELDS, MARK_READ, APPROVE_ORDER, REJECT_ORDER
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
        public final Map<String, String> additionalQueryParams;

        public DependencyRule(String parentField, String childField, String sourceModule, String queryParam, List<String> cascadeClearFields) {
            this(parentField, childField, sourceModule, queryParam, cascadeClearFields, Map.of());
        }

        public DependencyRule(String parentField, String childField, String sourceModule, String queryParam,
                              List<String> cascadeClearFields, Map<String, String> additionalQueryParams) {
            this.parentField = parentField;
            this.childField = childField;
            this.sourceModule = sourceModule;
            this.queryParam = queryParam;
            this.cascadeClearFields = cascadeClearFields;
            this.additionalQueryParams = additionalQueryParams == null ? Map.of() : additionalQueryParams;
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

    public static final class FormValueRule {
        public final String targetField;
        public final String sourceField;
        public final String defaultValue;
        public final String clearWhenBlankField;

        private FormValueRule(String targetField, String sourceField, String defaultValue, String clearWhenBlankField) {
            this.targetField = targetField;
            this.sourceField = sourceField;
            this.defaultValue = defaultValue;
            this.clearWhenBlankField = clearWhenBlankField;
        }

        public static FormValueRule copyIfBlank(String targetField, String sourceField) {
            return new FormValueRule(targetField, sourceField, null, null);
        }

        public static FormValueRule defaultIfBlank(String targetField, String defaultValue) {
            return new FormValueRule(targetField, null, defaultValue, null);
        }

        public static FormValueRule clearWhenBlank(String targetField, String clearWhenBlankField) {
            return new FormValueRule(targetField, null, null, clearWhenBlankField);
        }
    }

    private static final String MODULE_DEPT = "dept";
    private static final String MODULE_WAREHOUSE = "warehouse";
    private static final String MODULE_ROLE = "role";
    private static final String MODULE_PERMISSION = "permission";
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
    private static final String GOODS_SERIES_OPTIONS = "_goodsSeriesOptions";
    private static final String GOODS_MAKER_OPTIONS = "_goodsMakerOptions";
    private static final String DEFAULT_FIELD_TITLE = "項目";
    private static final Set<String> ALWAYS_HIDDEN_COLUMNS = Set.of("beforeqty", "afterqty");
    private static final Set<String> GOODS_HIDDEN_ID_COLUMNS = Set.of("brandid", "seriesid", "categoryid", "makerid");
    private static final Set<String> GOODS_DETAIL_ONLY_COLUMNS = Set.of("costprice", "updateprice", "priceupdatetime", "barcode", "weight", "volume", "imageurl");
    private static final Set<String> GOODS_READONLY_FIELDS = Set.of();

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
    private static final Map<String, List<String>> PREFERRED_COLUMNS = new HashMap<>();
    private static final Map<String, ModuleActionPolicy> ACTION_POLICIES = new HashMap<>();
    private static final Map<String, String> WRITE_PERMISSION_CODES = new HashMap<>();
    private static final Map<String, List<FormValueRule>> FORM_VALUE_RULES = new HashMap<>();
    private static final Map<String, Map<String, Map<String, String>>> INITIAL_RELATION_FILTERS = new HashMap<>();
    private static final List<String> INLINE_READONLY_FIELDS = List.of(
            "id", "createTime", "updateTime", "statusDesc", "beforeQty", "afterQty",
            "currentQty"
    );
    private static final ResourceBundle UI_BUNDLE = ResourceBundle.getBundle("i18n.ui", Locale.JAPAN);

    static {
        QUERY_FIELDS.put(USER, List.of("username", "deptId", "deptName", "roleId", "roleName", "email", "phone", "status"));
        QUERY_FIELDS.put(MODULE_DEPT, List.of(ID, "name", "code", "leaderId", "sort", "status"));
        QUERY_FIELDS.put(MODULE_WAREHOUSE, List.of(ID, "name", "code", "address", "managerId", "status"));
        QUERY_FIELDS.put(MODULE_ROLE, List.of(ID, "name", "code", "permissionNames", "remark", "status"));
        QUERY_FIELDS.put(MODULE_PERMISSION, List.of(ID, "name", "code", "module", "type", "parentId", "path", "sort", "icon", "component", "status"));
        QUERY_FIELDS.put(GOODS, List.of("name", "skuCode", "brandId", "seriesId", "categoryId", "makerId", "status"));
        QUERY_FIELDS.put(BRAND_HIERARCHY, List.of("brandName", "brandEnglishName", "seriesName", "seriesEnglishName", "makerName", "makerEnglishName", "status"));
        QUERY_FIELDS.put(MODULE_MAKER, List.of(ID, "name", "englishName", "seriesName", "brandName", "status"));
        QUERY_FIELDS.put(MODULE_BRAND, List.of(ID, "name", "englishName", "image", "content", "status"));
        QUERY_FIELDS.put(MODULE_CATEGORY, List.of(ID, "name", "status"));
        QUERY_FIELDS.put(SERIES, List.of(ID, "name", "englishName", "brandName", "content", "status"));
        QUERY_FIELDS.put(MODULE_STOCK, List.of(ID, "goodsId", "goodsName", "skuCode", "skuId", "stockTypeId", "currentQty", "lockQty", "bizDateSummary", "price", "priceUpdateTime", "currency", "warehouseId", "stockScope", "groupCode", "status"));
        QUERY_FIELDS.put(MODULE_STOCK_TYPE, List.of(ID, "name", "status"));
        QUERY_FIELDS.put(STOCK_ORDER, List.of(ID, "orderNo", "orderType", "bizDate", "stockCategory", "stockTypeId", "warehouseId", "sourceType", "sourceId", "totalQty", "state", "requesterId", "requesterName", "operatorId", "operatorName", "approverId", "approverName", "approveTime", "finishTime", "remark"));
        QUERY_FIELDS.put(STOCK_ORDER_ITEM, List.of(ID, "orderId", "goodsId", "skuId", "skuCode", "goodsName", "englishName", "brandId", "brandName", "seriesId", "seriesName", "categoryId", "categoryName", "stockTypeId", "stockTypeName", "makerId", "makerName", "changeQty", "price", "currency", "remark"));
        QUERY_FIELDS.put(REQUEST_FORM, List.of(ID, "bizNo", "sourceOrderId", "sourceOrderNo", "userId", "username", "deptId", "deptName", "customerId", "customerName", "warehouseId", "totalQty", "requestQty", "totalAmt", "state", "approverId", "approverName", "approveTime", "approveRemark"));
        QUERY_FIELDS.put(REQUEST_ITEM, List.of("customerId", "customerName", "goodsId", "goodsName", "skuCode", "stockTypeId", "startDate", "endDate"));
        QUERY_FIELDS.put(DELIVERY_SCHEDULE, List.of("customerId", "customerName", "categoryId", "goodsId", "goodsName", "skuCode", "stockTypeId", "startDate", "endDate"));
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
        FORM_FIELDS.put(MODULE_STOCK, List.of("warehouseId", "goodsId", "skuId", "sourceType", "outboundMode", "stockTypeId", "bizDate", "batchId", "quantity", "remark"));
        FORM_FIELDS.put(STOCK_ORDER, List.of("orderType", "bizDate", "warehouseId", "sourceType", "stockTypeId", "state", "remark"));
        FORM_FIELDS.put(STOCK_ORDER_ITEM, List.of("orderId", "goodsId", "skuId", "skuCode", "goodsName", "englishName", "brandId", "brandName", "seriesId", "seriesName", "categoryId", "categoryName", "stockTypeId", "stockTypeName", "makerId", "makerName", "changeQty", "price", "currency", "remark"));
        FORM_FIELDS.put(MODULE_STOCK_RECORD, List.of("bizNo", "orderId", "orderItemId", "stockId", "goodsId", "skuId", "skuCode", "goodsName", "englishName", "brandId", "brandName", "seriesId", "seriesName", "categoryId", "categoryName", "stockTypeId", "stockTypeName", "makerId", "makerName", "warehouseId", "changeQty", "sourceType", "orderType", "bizDate", "price", "currency", "priceUpdateTime", "customerId", "customerName", "requesterId", "requesterName", "operatorId", "operatorName", "remark"));
        FORM_FIELDS.put(REQUEST_FORM, List.of("bizNo", "sourceOrderId", "sourceOrderNo", "userId", "username", "deptId", "deptName", "customerId", "customerName", "warehouseId", "totalQty", "requestQty", "totalAmt", "state", "approverId", "approverName", "approveTime", "approveRemark"));
        FORM_FIELDS.put(REQUEST_ITEM, List.of("requestId", "goodsId", "skuId", "skuCode", "goodsName", "englishName", "brandId", "brandName", "seriesId", "seriesName", "categoryId", "categoryName", "makerId", "makerName", "stockTypeId", "stockTypeName", "warehouseId", "price", "discountPrice", "currency", "discount", "requestQty", "approveQty", "outQty", "depositAmt", "depositTime", "depositFee", "stockRecordId", "remark"));
        FORM_FIELDS.put(MODULE_WAREHOUSE, List.of("name", "code", "address", "managerId", "status"));
        FORM_FIELDS.put(MODULE_STOCK_TYPE, List.of("name", "status"));
        FORM_FIELDS.put(MODULE_ROLE, List.of("name", "code", "permissionIds", "remark", "status"));
        FORM_FIELDS.put(MODULE_MAKER, List.of("name", "englishName", "seriesId", "status"));
        FORM_FIELDS.put(MODULE_BRAND, List.of("name", "englishName", "image", "content", "status"));
        FORM_FIELDS.put(BRAND_HIERARCHY, List.of(
                "brandName", "brandEnglishName", "seriesName", "seriesEnglishName",
                "makerName", "makerEnglishName", "status"
        ));
        FORM_FIELDS.put(MODULE_CATEGORY, List.of("name", "status"));
        FORM_FIELDS.put(SERIES, List.of("name", "englishName", "brandId", "content", "status"));
        FORM_FIELDS.put(MODULE_CUSTOMER, List.of("customerCode", "name", "englishName", "contactPerson", "phone", "email", "country", "city", "address", "levelId", "ownerUserId", "ownerDeptId", "remark", "status"));

        REQUIRED_FORM_FIELDS.put(USER, List.of("username", "password", "deptId", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_DEPT, List.of("name", "code", "status"));
        REQUIRED_FORM_FIELDS.put(GOODS, List.of("name", "englishName", "brandId", "categoryId", "skuCode"));
        REQUIRED_FORM_FIELDS.put(MODULE_STOCK, List.of("goodsId", "skuId", "sourceType", "warehouseId", "stockTypeId", "quantity"));
        REQUIRED_FORM_FIELDS.put(STOCK_ORDER, List.of("orderType", "warehouseId"));
        REQUIRED_FORM_FIELDS.put(STOCK_ORDER_ITEM, List.of("orderId", "goodsId", "skuId", "goodsName", "changeQty"));
        REQUIRED_FORM_FIELDS.put(MODULE_STOCK_RECORD, List.of("bizNo", "orderId", "orderItemId", "stockId", "goodsId", "skuId", "goodsName", "changeQty", "orderType", "sourceType"));
        REQUIRED_FORM_FIELDS.put(REQUEST_FORM, List.of("bizNo", "sourceOrderId", "userId", "username", "customerId", "customerName"));
        REQUIRED_FORM_FIELDS.put(REQUEST_ITEM, List.of("requestId", "goodsId", "skuId"));
        REQUIRED_FORM_FIELDS.put(MODULE_WAREHOUSE, List.of("name", "code", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_ROLE, List.of("name", "code", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_MAKER, List.of("name", "seriesId", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_BRAND, List.of("name", "status"));
        REQUIRED_FORM_FIELDS.put(BRAND_HIERARCHY, List.of("brandName", "seriesName", "makerName", "status"));
        REQUIRED_FORM_FIELDS.put(MODULE_CATEGORY, List.of("name", "status"));
        REQUIRED_FORM_FIELDS.put(SERIES, List.of("name", "brandId", "status"));

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
        RELATION_FIELD_MODULE.put("batchId", "_stockBatchOptions");
        RELATION_FIELD_MODULE.put("customerId", MODULE_CUSTOMER);
        RELATION_FIELD_MODULE.put("levelId", MODULE_CUSTOMER_LEVEL);
        RELATION_FIELD_MODULE.put("ownerUserId", USER);
        RELATION_FIELD_MODULE.put("ownerDeptId", MODULE_DEPT);
        RELATION_FIELD_MODULE.put("userId", USER);
        RELATION_FIELD_MODULE.put("requesterId", USER);
        RELATION_FIELD_MODULE.put("operatorId", USER);
        RELATION_FIELD_MODULE.put("approverId", USER);

        NAME_TO_ID_FIELD.put("deptName", "deptId");
        NAME_TO_ID_FIELD.put("parentName", "parentId");
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
        NAME_TO_ID_FIELD.put("userName", "userId");
        NAME_TO_ID_FIELD.put("requesterName", "requesterId");
        NAME_TO_ID_FIELD.put("operatorName", "operatorId");
        NAME_TO_ID_FIELD.put("approverName", "approverId");
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
        putOptions(GOODS + ".isHot", List.of(new Option("はい", "1"), new Option("いいえ", "0")));
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
                new Option("自社入庫（承認必須）", "1"),
                new Option("再販売入庫（即時入庫）", "2")
        ));
        putOptions(MODULE_STOCK + ".stockScope", List.of(
                new Option("全部", "all"),
                new Option("自社在庫", "self"),
                new Option("组别在库", "group")
        ));
        putOptions(MODULE_STOCK_RECORD + ".orderType", List.of(
                new Option("入庫", "1"), new Option("出庫", "2"), new Option("調整", "3"),
                new Option("棚卸", "4"), new Option("移動", "5"), new Option("返品", "6")
        ));
        putOptions(MODULE_STOCK_RECORD + ".sourceType", List.of(
                new Option("注文", "1"), new Option("返品", "2"), new Option("申請書", "3"), new Option("手動", "4")
        ));
        putOptions(MODULE_STOCK_RECORD + ".state", List.of(
                new Option("草稿", "0"), new Option("審査中", "1"), new Option("完了", "2"), new Option("取消", "3")
        ));
        putOptions(MODULE_MESSAGE + ".isRead", List.of(
                new Option("未読", "0"), new Option("既読", "1")
        ));
        putOptions(MODULE_MESSAGE + ".state", List.of(
                new Option("未読", "0"), new Option("既読", "1")
        ));

        DEPENDENCY_RULES.put(GOODS, List.of(
                new DependencyRule("brandId", "seriesId", GOODS_SERIES_OPTIONS, "brandId", List.of("makerId", "goodsId", "skuId")),
                new DependencyRule("seriesId", "makerId", GOODS_MAKER_OPTIONS, "seriesId", List.of("goodsId", "skuId"),
                        Map.of("brandId", "brandId")),
                new DependencyRule("seriesId", "goodsId", GOODS, "seriesId", List.of("skuId")),
                new DependencyRule("categoryId", "goodsId", GOODS, "categoryId", List.of("skuId")),
                new DependencyRule("goodsId", "skuId", GOODS_SKU, "goodsId", List.of())
        ));
        DEPENDENCY_RULES.put(MODULE_STOCK, List.of(
                new DependencyRule("goodsId", "skuId", GOODS_SKU, "goodsId", List.of())
        ));
        DEPENDENCY_RULES.put(REQUEST_ITEM, List.of(new DependencyRule("goodsId", "skuId", GOODS_SKU, "goodsId", List.of())));
        DEPENDENCY_RULES.put(STOCK_ORDER_ITEM, List.of(new DependencyRule("goodsId", "skuId", GOODS_SKU, "goodsId", List.of())));

        ROW_ACTIONS.put(STOCK_ORDER, List.of(
                new RowAction(RowActionType.DETAIL, "action.orderDetail", STOCK_ORDER_ITEM, "orderId"),
                new RowAction(RowActionType.APPROVE_ORDER, "action.approve", null, null),
                new RowAction(RowActionType.REJECT_ORDER, "action.reject", null, null)
        ));
        ROW_ACTIONS.put(REQUEST_FORM, List.of(
                new RowAction(RowActionType.DETAIL, "action.requestDetail", REQUEST_ITEM, "requestId"),
                new RowAction(RowActionType.MATCH_REQUEST_ITEMS, "action.matchRequestItems", null, null),
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

        HIDDEN_LIST_FIELDS.put(GOODS, List.of(
                "sort", "isHot", "skuStatus", "skuStatusDesc", "imageSort", "currentQty", "lockQty"
        ));
        HIDDEN_LIST_FIELDS.put(GOODS_SKU, List.of("costPrice", "updatePrice", "priceUpdateTime", "barcode", "weight", "volume"));
        HIDDEN_LIST_FIELDS.put(MODULE_GOODS_IMAGE, List.of("imageUrl"));
        HIDDEN_LIST_FIELDS.put(DELIVERY_SCHEDULE, List.of(
                "__level", "__nodeType", "recordId", "orderId", "orderItemId", "customerId", "goodsId", "skuId", "stockTypeId"
        ));
        HIDDEN_LIST_FIELDS.put(REQUEST_ITEM, List.of(
                "stockRecordId", "stockRecordIds", "stockOrderId", "stockOrderItemId", "stockOrderItemIds",
                "customerId", "goodsId", "skuId",
                "brandId", "seriesId", "makerId", "stockTypeId", "orderType", "state",
                "requestItemState", "requestItemId", "selected", "knife", "handle", "handleCandidates"
        ));
        HIDDEN_LIST_FIELDS.put(BRAND_HIERARCHY, List.of("id", "nodeType", "brandId", "seriesId", "makerId"));
        PREFERRED_COLUMNS.put(GOODS, List.of(
                "skuId", "goodsName", "name", "goodsId", "englishName", "customerCode", "brandName", "seriesName",
                "categoryName", "makerName", "stockTypeName", "skuCode", "skuName", "specSummary", "barcode",
                "weight", "volume", "price", "costPrice", "updatePrice", "oldPrice", "newPrice", "discount",
                "currency", "lockQty", "beforeQty", "changeQty", "afterQty", "statusDesc", "status",
                "mainImage", "imageUrl", "priceUpdateTime", "effectiveTime", "expireTime", "remark", "description"
        ));

        ACTION_POLICIES.put(MODULE_STOCK_RECORD, new ModuleActionPolicy(false, false, false, true, true));
        ACTION_POLICIES.put(MODULE_PRICE_RECORD, new ModuleActionPolicy(false, false, false, true, true));
        ACTION_POLICIES.put(MODULE_OPERATE_LOG, new ModuleActionPolicy(false, false, false, false, false));
        ACTION_POLICIES.put(MODULE_MESSAGE, new ModuleActionPolicy(true, true, true, true, true));
        ACTION_POLICIES.put(GOODS, new ModuleActionPolicy(true, false, true, true, true));
        ACTION_POLICIES.put(DELIVERY_SCHEDULE, new ModuleActionPolicy(false, false, false, false, false));
        ACTION_POLICIES.put(BRAND_HIERARCHY, new ModuleActionPolicy(true, false, true, false, true));
        ACTION_POLICIES.put(REQUEST_ITEM, new ModuleActionPolicy(true, false, false, false, false));

        WRITE_PERMISSION_CODES.put(STOCK_ORDER, "DATA_STOCK_ORDER_WRITE");
        WRITE_PERMISSION_CODES.put(STOCK_ORDER_ITEM, "DATA_STOCK_ORDER_ITEM_WRITE");
        WRITE_PERMISSION_CODES.put(REQUEST_FORM, "DATA_REQUEST_FORM_WRITE");
        WRITE_PERMISSION_CODES.put(REQUEST_ITEM, "DATA_REQUEST_ITEM_WRITE");
        WRITE_PERMISSION_CODES.put(MODULE_CUSTOMER, "DATA_CUSTOMER_WRITE");
        WRITE_PERMISSION_CODES.put(MODULE_CUSTOMER_LEVEL, "DATA_CUSTOMER_LEVEL_WRITE");
        WRITE_PERMISSION_CODES.put(MODULE_MESSAGE, "DATA_MESSAGE_WRITE");
        WRITE_PERMISSION_CODES.put(BRAND_HIERARCHY, "DATA_BRAND_WRITE");

        FORM_VALUE_RULES.put(GOODS, List.of(
                FormValueRule.copyIfBlank("skuName", "name"),
                FormValueRule.defaultIfBlank("currency", "JPY"),
                FormValueRule.clearWhenBlank("priceUpdateTime", "updatePrice")
        ));
        FORM_VALUE_RULES.put(MODULE_STOCK, List.of(
                FormValueRule.defaultIfBlank("sourceType", "1")
        ));
        FORM_VALUE_RULES.put(STOCK_ORDER, List.of(
                FormValueRule.defaultIfBlank("sourceType", "4"),
                FormValueRule.defaultIfBlank("state", "0")
        ));
        INITIAL_RELATION_FILTERS.put(MODULE_STOCK, Map.of(
                "warehouseId", Map.of("name", "自社在庫")
        ));
        INITIAL_RELATION_FILTERS.put(STOCK_ORDER, Map.of(
                "stockTypeId", Map.of("name", "通常品")
        ));
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

    public static List<String> visibleQueryFields(String moduleKey) {
        List<String> fields = new ArrayList<>(queryFields(moduleKey));
        if (STOCK_ORDER_ITEM.equals(moduleKey)) {
            fields.remove("orderId");
        }
        return fields;
    }

    public static List<String> formFields(String moduleKey) {
        return FORM_FIELDS.getOrDefault(moduleKey, List.of());
    }

    public static List<String> resolvedFormFields(String moduleKey, Iterable<String> availableFields) {
        List<String> configuredFields = formFields(moduleKey);
        if (!configuredFields.isEmpty()) {
            return configuredFields;
        }
        List<String> resolvedFields = normalizeFallbackFormFields(availableFields);
        return resolvedFields.isEmpty() ? normalizeFallbackFormFields(queryFields(moduleKey)) : resolvedFields;
    }

    private static List<String> normalizeFallbackFormFields(Iterable<String> fields) {
        if (fields == null) {
            return List.of();
        }
        Set<String> normalizedFields = new LinkedHashSet<>();
        for (String field : fields) {
            if (field == null || field.isBlank() || field.startsWith("__")) {
                continue;
            }
            String mappedField = mapNameFieldToIdField(field);
            String normalizedField = mappedField.isBlank() ? field : mappedField;
            if (!isReadonlyFormField(normalizedField)) {
                normalizedFields.add(normalizedField);
            }
        }
        return new ArrayList<>(normalizedFields);
    }

    private static boolean isReadonlyFormField(String field) {
        String low = field.trim().toLowerCase(Locale.ROOT);
        return "id".equals(low)
                || "createtime".equals(low)
                || "updatetime".equals(low)
                || "statusdesc".equals(low)
                || GOODS_READONLY_FIELDS.contains(low)
                || "beforeqty".equals(low)
                || "afterqty".equals(low);
    }

    public static boolean isRequiredFormField(String moduleKey, String field) {
        return REQUIRED_FORM_FIELDS.getOrDefault(moduleKey, List.of()).contains(field);
    }

    public static List<String> orderedColumns(String moduleKey, Iterable<String> keys) {
        List<String> rawKeys = new ArrayList<>();
        for (String key : keys) {
            if (key != null && !key.isBlank()) {
                rawKeys.add(key);
            }
        }
        if (rawKeys.isEmpty()) {
            return List.of();
        }

        Set<String> lowerKeySet = new HashSet<>();
        for (String key : rawKeys) {
            lowerKeySet.add(key.toLowerCase(Locale.ROOT));
        }

        List<String> filtered = new ArrayList<>();
        for (String key : rawKeys) {
            String low = key.toLowerCase(Locale.ROOT);
            if ("__selected".equals(low)) {
                continue;
            }
            if (HIDDEN_LIST_FIELDS.getOrDefault(moduleKey, List.of()).contains(key)) {
                continue;
            }
            if (ALWAYS_HIDDEN_COLUMNS.contains(low)) {
                continue;
            }
            if ("status".equals(low) && lowerKeySet.contains("statusdesc")) {
                continue;
            }
            if (GOODS.equals(moduleKey) && ("id".equals(low) || "imageid".equals(low)
                    || GOODS_HIDDEN_ID_COLUMNS.contains(low) || GOODS_DETAIL_ONLY_COLUMNS.contains(low))) {
                continue;
            }
            if (shouldHideIdField(key, low, lowerKeySet)) {
                continue;
            }
            filtered.add(key);
        }

        if (GOODS.equals(moduleKey)) {
            return sortGoodsColumns(filtered);
        }
        if (DELIVERY_SCHEDULE.equals(moduleKey)) {
            return sortDeliveryScheduleColumns(filtered);
        }
        if (REQUEST_ITEM.equals(moduleKey)) {
            return sortRequestItemCartColumns(filtered);
        }
        if (BRAND_HIERARCHY.equals(moduleKey)) {
            return sortBrandHierarchyColumns(filtered);
        }
        return sortDefaultColumns(filtered);
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

    public static Map<String, String> initialRelationFilters(String moduleKey, String field) {
        return INITIAL_RELATION_FILTERS.getOrDefault(moduleKey, Map.of()).getOrDefault(field, Map.of());
    }

    public static boolean shouldAutoSelectFirstRelation(String moduleKey, String field) {
        return !initialRelationFilters(moduleKey, field).isEmpty();
    }

    public static String mapNameFieldToIdField(String field) {
        return NAME_TO_ID_FIELD.getOrDefault(field, "");
    }

    public static List<Option> selectOptions(String moduleKey, String field) {
        List<Option> moduleOptions = SELECT_OPTIONS.get(moduleKey + "." + field);
        if (moduleOptions != null) {
            if (MODULE_STOCK.equals(moduleKey) && "sourceType".equals(field)) {
                return moduleOptions.stream()
                        .filter(option -> !"2".equals(option.value))
                        .toList();
            }
            if (STOCK_ORDER.equals(moduleKey) && Session.isNormalUser()
                    && ("sourceType".equals(field) || "state".equals(field))) {
                return moduleOptions.stream()
                        .filter(option -> "sourceType".equals(field)
                                ? "3".equals(option.value) || "4".equals(option.value)
                                : "0".equals(option.value) || "1".equals(option.value))
                        .toList();
            }
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
        return autoLabelFromField(key);
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

    public static boolean canWriteByPermission(String moduleKey) {
        if (MODULE_STOCK_RECORD.equals(moduleKey) || MODULE_PRICE_RECORD.equals(moduleKey)) {
            return false;
        }
        if (Session.hasPermission("DATA_ALL_WRITE")) {
            return true;
        }
        if (MODULE_STOCK.equals(moduleKey)) {
            return hasAnyPermission(
                    "DATA_STOCK_WRITE",
                    "DATA_STOCK_SELF_WRITE",
                    "DATA_STOCK_A_WRITE",
                    "DATA_STOCK_B_WRITE",
                    "DATA_STOCK_C_WRITE",
                    "DATA_STOCK_CUSTOMER_WRITE",
                    "DATA_STOCK_ORDER_WRITE",
                    "DATA_STOCK_ORDER_ITEM_WRITE",
                    "DATA_STOCK_TYPE_WRITE");
        }
        String code = WRITE_PERMISSION_CODES.get(moduleKey);
        if (code == null || code.isBlank()) {
            code = "DATA_" + camelToUpperUnderscore(moduleKey) + "_WRITE";
        }
        return Session.hasPermission(code);
    }

    private static boolean hasAnyPermission(String... codes) {
        if (codes == null || codes.length == 0) {
            return false;
        }
        for (String code : codes) {
            if (code != null && !code.isBlank() && Session.hasPermission(code)) {
                return true;
            }
        }
        return false;
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

    public static boolean isReadonlyPayloadField(String field) {
        if (field == null || field.isBlank()) {
            return true;
        }
        String low = field.trim().toLowerCase(Locale.ROOT);
        return !mapNameFieldToIdField(field).isBlank()
                || "createtime".equals(low)
                || "updatetime".equals(low)
                || "statusdesc".equals(low)
                || GOODS_READONLY_FIELDS.contains(low)
                || "beforeqty".equals(low)
                || "afterqty".equals(low);
    }

    public static String updatePayloadField(String moduleKey, String field) {
        if (field == null || field.isBlank()) {
            return "";
        }
        if (GOODS.equals(moduleKey) && "goodsName".equals(field)) {
            return "name";
        }
        return isReadonlyPayloadField(field) ? "" : field;
    }

    public static JSONObject applyFormValueRules(String moduleKey, JSONObject dto) {
        if (dto == null) {
            return new JSONObject();
        }
        for (FormValueRule rule : FORM_VALUE_RULES.getOrDefault(moduleKey, List.of())) {
            if (rule.defaultValue != null) {
                if (isBlank(dto.opt(rule.targetField))) {
                    dto.put(rule.targetField, rule.defaultValue);
                }
                continue;
            }
            if (rule.sourceField != null) {
                if (isBlank(dto.opt(rule.targetField)) && !isBlank(dto.opt(rule.sourceField))) {
                    dto.put(rule.targetField, String.valueOf(dto.opt(rule.sourceField)).trim());
                }
                continue;
            }
            if (rule.clearWhenBlankField != null && isBlank(dto.opt(rule.clearWhenBlankField))) {
                dto.remove(rule.targetField);
            }
        }
        return dto;
    }

    private static boolean isBlank(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty();
    }

    private static boolean shouldHideIdField(String key, String low, Set<String> lowerKeySet) {
        if ("id".equals(low)) {
            return false;
        }
        if (!(low.endsWith("id") || low.endsWith("ids"))) {
            return false;
        }

        String base = low.endsWith("ids") ? low.substring(0, low.length() - 3) : low.substring(0, low.length() - 2);
        if (lowerKeySet.contains(base + "name") || lowerKeySet.contains(base + "names")) {
            return true;
        }

        for (Map.Entry<String, String> entry : NAME_TO_ID_FIELD.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(key) && lowerKeySet.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return RELATION_FIELD_MODULE.containsKey(key) && lowerKeySet.contains((base + "name").toLowerCase(Locale.ROOT));
    }

    private static List<String> sortDefaultColumns(List<String> columns) {
        if (columns.isEmpty()) {
            return columns;
        }
        List<String> head = new ArrayList<>();
        List<String> tail = new ArrayList<>();
        boolean hasId = false;
        for (String key : columns) {
            String low = key.toLowerCase(Locale.ROOT);
            if ("id".equals(low)) {
                hasId = true;
                continue;
            }
            if ("createtime".equals(low) || "updatetime".equals(low)) {
                tail.add(key);
            } else {
                head.add(key);
            }
        }

        List<String> sorted = new ArrayList<>();
        if (hasId) {
            sorted.add(ID);
        }
        sorted.addAll(head);
        sorted.addAll(tail);
        return sorted;
    }

    private static List<String> sortGoodsColumns(List<String> columns) {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (String key : PREFERRED_COLUMNS.getOrDefault(GOODS, List.of())) {
            if (containsIgnoreCase(columns, key)) {
                ordered.add(findOriginalKey(columns, key));
            }
        }
        for (String key : columns) {
            ordered.add(key);
        }
        return new ArrayList<>(ordered);
    }

    private static List<String> sortDeliveryScheduleColumns(List<String> columns) {
        List<String> preferred = List.of(
                "country", "customerName", "groupCode", "outboundDate", "bizNo", "goodsName",
                "skuCode", "brandName", "seriesName", "makerName", "categoryName",
                "stockTypeName", "quantity", "availableQty", "requestQty", "price", "currency", "operatorName"
        );
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (String key : preferred) {
            if (containsIgnoreCase(columns, key)) {
                ordered.add(findOriginalKey(columns, key));
            }
        }
        for (String key : columns) {
            ordered.add(key);
        }
        return new ArrayList<>(ordered);
    }

    private static List<String> sortRequestItemCartColumns(List<String> columns) {
        List<String> preferred = List.of(
                "country", "customerName", "groupCode", "bizDate", "orderNo", "goodsName", "skuCode",
                "brandName", "seriesName", "makerName", "categoryName", "stockTypeName",
                "availableQty", "requestQty", "price", "currency", "operatorName"
        );
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (String key : preferred) {
            if (containsIgnoreCase(columns, key)) {
                ordered.add(findOriginalKey(columns, key));
            }
        }
        for (String key : columns) {
            ordered.add(key);
        }
        return new ArrayList<>(ordered);
    }

    private static List<String> sortBrandHierarchyColumns(List<String> columns) {
        List<String> preferred = List.of(
                "brandName", "brandEnglishName", "seriesName", "seriesEnglishName",
                "makerName", "makerEnglishName", "status", "updateTime"
        );
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (String key : preferred) {
            if (containsIgnoreCase(columns, key)) {
                ordered.add(findOriginalKey(columns, key));
            }
        }
        for (String key : columns) {
            ordered.add(key);
        }
        return new ArrayList<>(ordered);
    }

    private static boolean containsIgnoreCase(List<String> keys, String target) {
        for (String key : keys) {
            if (key.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private static String findOriginalKey(List<String> keys, String target) {
        for (String key : keys) {
            if (key.equalsIgnoreCase(target)) {
                return key;
            }
        }
        return target;
    }

    private static String camelToUpperUnderscore(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
    }

    private static String autoLabelFromField(String key) {
        if (key == null || key.isBlank()) {
            return DEFAULT_FIELD_TITLE;
        }
        String text = key.trim();
        String lower = text.toLowerCase(Locale.ROOT);
        if ("id".equals(lower)) return "ID";
        if ("createtime".equals(lower)) return "作成日時";
        if ("updatetime".equals(lower)) return "更新日時";
        if ("statusdesc".equals(lower) || "status".equals(lower)) return "状態";
        if (text.endsWith("Names")) return readable(text.substring(0, text.length() - 5)) + "名";
        if (text.endsWith("Name")) return readable(text.substring(0, text.length() - 4)) + "名";
        if (text.endsWith("Ids")) return readable(text.substring(0, text.length() - 3)) + "ID一覧";
        if (text.endsWith("Id")) return readable(text.substring(0, text.length() - 2)) + "ID";
        if (text.endsWith("Code")) return readable(text.substring(0, text.length() - 4)) + "コード";
        if (text.endsWith("Time") || text.endsWith("Date")) {
            return readable(text.replaceAll("(Time|Date)$", "")) + "日時";
        }
        return readable(text);
    }

    private static String readable(String value) {
        String text = String.valueOf(value)
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .trim();
        if (text.isEmpty()) {
            return DEFAULT_FIELD_TITLE;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}

