package co.handk.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModuleMeta {

    private ModuleMeta() {
    }

    public enum FieldType {
        TEXT, NUMBER, SELECT, RELATION
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

    private static final Map<String, List<String>> QUERY_FIELDS = new HashMap<>();
    private static final Map<String, List<String>> FORM_FIELDS = new HashMap<>();
    private static final Map<String, List<String>> REQUIRED_FORM_FIELDS = new HashMap<>();
    private static final Map<String, Map<String, FieldType>> FIELD_TYPES = new HashMap<>();
    private static final Map<String, String> RELATION_FIELD_MODULE = new HashMap<>();
    private static final Map<String, String> NAME_TO_ID_FIELD = new HashMap<>();
    private static final Map<String, String> FIELD_LABELS = new HashMap<>();
    private static final Map<String, List<Option>> SELECT_OPTIONS = new HashMap<>();
    private static final Map<String, List<DependencyRule>> DEPENDENCY_RULES = new HashMap<>();

    static {
        QUERY_FIELDS.put("user", List.of("username", "deptId", "deptName", "email", "phone", "status"));
        QUERY_FIELDS.put("dept", List.of("id", "name", "code", "leaderId", "sort", "status"));
        QUERY_FIELDS.put("warehouse", List.of("id", "name", "code", "address", "managerId", "status"));
        QUERY_FIELDS.put("role", List.of("id", "name", "code", "remark", "status"));
        QUERY_FIELDS.put("permission", List.of("id", "name", "code", "module", "type", "status"));
        QUERY_FIELDS.put("goods", List.of("id", "name", "englishName", "seriesId", "brandId", "categoryId", "makerId", "sort", "status", "isHot"));
        QUERY_FIELDS.put("goodsLevelPrice", List.of("id", "goodsId", "skuId", "levelId", "price", "currency", "discount", "status"));
        QUERY_FIELDS.put("maker", List.of("id", "name", "status"));
        QUERY_FIELDS.put("brand", List.of("id", "name", "englishName", "status"));
        QUERY_FIELDS.put("category", List.of("id", "name", "status"));
        QUERY_FIELDS.put("series", List.of("id", "name", "englishName", "brandId", "status"));
        QUERY_FIELDS.put("stock", List.of("id", "goodsId", "goodsName", "skuCode", "skuId", "stockTypeId", "currentQty", "lockQty", "price", "currency", "warehouseId", "status"));
        QUERY_FIELDS.put("stockType", List.of("id", "name", "status"));
        QUERY_FIELDS.put("stockOrder", List.of("id", "orderNo", "orderType", "stockTypeId", "warehouseId", "sourceType", "sourceId", "totalQty", "state", "requesterId", "operatorId", "approverId", "approveTime", "finishTime", "remark"));
        QUERY_FIELDS.put("stockOrderItem", List.of("id", "orderId", "goodsId", "skuId", "skuCode", "goodsName", "beforeQty", "changeQty", "afterQty", "price", "currency", "remark"));
        QUERY_FIELDS.put("requestForm", List.of("id", "bizNo", "userId", "username", "deptId", "customerId", "customerName", "warehouseId", "totalQty", "requestQty", "totalAmt", "state", "approverId", "approveTime", "approveRemark"));
        QUERY_FIELDS.put("requestItem", List.of("id", "requestId", "goodsId", "skuId", "skuCode", "goodsName", "price", "currency", "discount", "requestQty", "approveQty", "outQty", "remark"));

        FORM_FIELDS.put("user", List.of("username", "password", "deptId", "email", "phone", "status"));
        FORM_FIELDS.put("dept", List.of("parentId", "name", "code", "leaderId", "sort", "status"));
        FORM_FIELDS.put("goods", List.of("name", "englishName", "brandId", "seriesId", "categoryId", "makerId", "description", "isHot", "skuCode", "skuName", "price", "status"));
        FORM_FIELDS.put("stock", List.of("goodsId", "sourceType", "warehouseId", "stockTypeId", "quantity", "remark", "status"));
        FORM_FIELDS.put("stockOrder", List.of("orderNo", "orderType", "warehouseId", "sourceType", "sourceId", "totalQty", "stockTypeId", "state", "requesterId", "operatorId", "approverId", "approveTime", "finishTime", "remark"));
        FORM_FIELDS.put("stockOrderItem", List.of("orderId", "goodsId", "skuId", "goodsName", "beforeQty", "changeQty", "afterQty", "price", "currency", "remark"));
        FORM_FIELDS.put("requestForm", List.of("bizNo", "userId", "username", "deptId", "customerId", "customerName", "warehouseId", "totalQty", "requestQty", "totalAmt", "state", "approverId", "approveTime", "approveRemark"));
        FORM_FIELDS.put("requestItem", List.of("requestId", "goodsId", "skuId", "skuCode", "goodsName", "price", "currency", "discount", "requestQty", "approveQty", "outQty", "remark"));
        FORM_FIELDS.put("warehouse", List.of("name", "code", "address", "managerId", "status"));
        FORM_FIELDS.put("stockType", List.of("name", "status"));
        FORM_FIELDS.put("role", List.of("name", "code", "remark", "status"));
        FORM_FIELDS.put("maker", List.of("name", "status"));
        FORM_FIELDS.put("brand", List.of("name", "englishName", "content", "status"));
        FORM_FIELDS.put("category", List.of("name", "status"));
        FORM_FIELDS.put("series", List.of("name", "englishName", "brandId", "content", "status"));

        REQUIRED_FORM_FIELDS.put("user", List.of("username", "password", "deptId", "status"));
        REQUIRED_FORM_FIELDS.put("dept", List.of("name", "code", "status"));
        REQUIRED_FORM_FIELDS.put("goods", List.of("name", "englishName", "brandId", "seriesId", "categoryId", "makerId", "skuCode", "skuName"));
        REQUIRED_FORM_FIELDS.put("stock", List.of("goodsId", "sourceType", "warehouseId", "stockTypeId", "quantity"));
        REQUIRED_FORM_FIELDS.put("stockOrder", List.of("orderNo", "orderType", "warehouseId", "sourceType"));
        REQUIRED_FORM_FIELDS.put("stockOrderItem", List.of("orderId", "goodsId", "skuId", "goodsName", "beforeQty", "changeQty", "afterQty"));
        REQUIRED_FORM_FIELDS.put("requestForm", List.of("bizNo", "userId", "username", "customerId", "customerName"));
        REQUIRED_FORM_FIELDS.put("requestItem", List.of("requestId", "goodsId", "skuId"));
        REQUIRED_FORM_FIELDS.put("warehouse", List.of("name", "code", "status"));
        REQUIRED_FORM_FIELDS.put("role", List.of("name", "code", "status"));
        REQUIRED_FORM_FIELDS.put("maker", List.of("name", "status"));
        REQUIRED_FORM_FIELDS.put("brand", List.of("name", "status"));
        REQUIRED_FORM_FIELDS.put("category", List.of("name", "status"));
        REQUIRED_FORM_FIELDS.put("series", List.of("name", "brandId", "status"));

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
        RELATION_FIELD_MODULE.put("stockTypeId", "stockType");
        RELATION_FIELD_MODULE.put("warehouseId", "warehouse");
        RELATION_FIELD_MODULE.put("customerId", "customer");
        RELATION_FIELD_MODULE.put("userId", "user");
        RELATION_FIELD_MODULE.put("requesterId", "user");
        RELATION_FIELD_MODULE.put("operatorId", "user");
        RELATION_FIELD_MODULE.put("approverId", "user");

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
        NAME_TO_ID_FIELD.put("stockTypeName", "stockTypeId");
        NAME_TO_ID_FIELD.put("warehouseName", "warehouseId");
        NAME_TO_ID_FIELD.put("customerName", "customerId");

        FIELD_LABELS.put("id", "ID");
        FIELD_LABELS.put("username", "ユーザー名");
        FIELD_LABELS.put("password", "パスワード");
        FIELD_LABELS.put("deptId", "部署");
        FIELD_LABELS.put("deptName", "部署名");
        FIELD_LABELS.put("name", "名称");
        FIELD_LABELS.put("englishName", "英語名");
        FIELD_LABELS.put("code", "コード");
        FIELD_LABELS.put("email", "メールアドレス");
        FIELD_LABELS.put("phone", "電話番号");
        FIELD_LABELS.put("warehouseId", "倉庫");
        FIELD_LABELS.put("warehouseName", "倉庫名");
        FIELD_LABELS.put("goodsId", "商品");
        FIELD_LABELS.put("goodsName", "商品名");
        FIELD_LABELS.put("skuId", "SKU");
        FIELD_LABELS.put("skuName", "SKU名");
        FIELD_LABELS.put("skuCode", "SKUコード");
        FIELD_LABELS.put("categoryId", "カテゴリ");
        FIELD_LABELS.put("categoryName", "カテゴリ名");
        FIELD_LABELS.put("brandId", "ブランド");
        FIELD_LABELS.put("brandName", "ブランド名");
        FIELD_LABELS.put("makerId", "メーカー");
        FIELD_LABELS.put("makerName", "メーカー名");
        FIELD_LABELS.put("seriesId", "シリーズ");
        FIELD_LABELS.put("seriesName", "シリーズ名");
        FIELD_LABELS.put("stockTypeId", "在庫区分");
        FIELD_LABELS.put("stockTypeName", "在庫区分");
        FIELD_LABELS.put("customerId", "顧客ID");
        FIELD_LABELS.put("customerName", "顧客名");
        FIELD_LABELS.put("customerCode", "顧客コード");
        FIELD_LABELS.put("orderNo", "伝票番号");
        FIELD_LABELS.put("orderId", "伝票ID");
        FIELD_LABELS.put("orderItemId", "伝票明細ID");
        FIELD_LABELS.put("requestId", "申請ID");
        FIELD_LABELS.put("bizNo", "業務番号");
        FIELD_LABELS.put("requestNo", "申請番号");
        FIELD_LABELS.put("requestType", "申請種別");
        FIELD_LABELS.put("requestStatus", "申請状態");
        FIELD_LABELS.put("orderType", "伝票種別");
        FIELD_LABELS.put("sourceType", "入出庫種別");
        FIELD_LABELS.put("sourceId", "参照ID");
        FIELD_LABELS.put("state", "状態");
        FIELD_LABELS.put("status", "ステータス");
        FIELD_LABELS.put("beforeQty", "処理前数量");
        FIELD_LABELS.put("changeQty", "変動数量");
        FIELD_LABELS.put("afterQty", "処理後数量");
        FIELD_LABELS.put("currentQty", "現在数量");
        FIELD_LABELS.put("lockQty", "引当数量");
        FIELD_LABELS.put("requestQty", "申請数量");
        FIELD_LABELS.put("approveQty", "承認数量");
        FIELD_LABELS.put("outQty", "出庫数量");
        FIELD_LABELS.put("totalQty", "合計数量");
        FIELD_LABELS.put("totalAmt", "合計金額");
        FIELD_LABELS.put("amount", "金額");
        FIELD_LABELS.put("price", "価格");
        FIELD_LABELS.put("currency", "通貨");
        FIELD_LABELS.put("discount", "割引率");
        FIELD_LABELS.put("address", "住所");
        FIELD_LABELS.put("country", "国");
        FIELD_LABELS.put("city", "都市");
        FIELD_LABELS.put("contactPerson", "担当者");
        FIELD_LABELS.put("remark", "備考");
        FIELD_LABELS.put("approveRemark", "承認備考");
        FIELD_LABELS.put("approveTime", "承認日時");
        FIELD_LABELS.put("finishTime", "完了日時");
        FIELD_LABELS.put("effectiveTime", "有効開始");
        FIELD_LABELS.put("expireTime", "有効終了");
        FIELD_LABELS.put("createTime", "作成日時");
        FIELD_LABELS.put("updateTime", "更新日時");
        FIELD_LABELS.put("priceUpdateTime", "価格更新日時");
        FIELD_LABELS.put("oldPrice", "旧価格");
        FIELD_LABELS.put("newPrice", "新価格");
        FIELD_LABELS.put("isHot", "おすすめ");
        FIELD_LABELS.put("isRead", "既読");
        FIELD_LABELS.put("leaderId", "責任者");
        FIELD_LABELS.put("leaderName", "責任者名");
        FIELD_LABELS.put("managerId", "管理者");
        FIELD_LABELS.put("managerName", "管理者名");
        FIELD_LABELS.put("requesterId", "申請者");
        FIELD_LABELS.put("requesterName", "申請者名");
        FIELD_LABELS.put("operatorId", "担当者");
        FIELD_LABELS.put("operatorName", "担当者名");
        FIELD_LABELS.put("approverId", "承認者");
        FIELD_LABELS.put("approverName", "承認者名");
        FIELD_LABELS.put("module", "モジュール");
        FIELD_LABELS.put("type", "タイプ");
        FIELD_LABELS.put("sort", "並び順");
        FIELD_LABELS.put("description", "説明");
        FIELD_LABELS.put("content", "内容");
        FIELD_LABELS.put("title", "タイトル");
        FIELD_LABELS.put("group", "グループ");
        FIELD_LABELS.put("value", "値");
        FIELD_LABELS.put("tip", "ヒント");
        FIELD_LABELS.put("message", "メッセージ");
        FIELD_LABELS.put("parentId", "親ID");
        FIELD_LABELS.put("levelId", "会員ランク");
        FIELD_LABELS.put("levelName", "ランク名");
        FIELD_LABELS.put("ownerUserId", "担当ユーザーID");
        FIELD_LABELS.put("ownerUserName", "担当ユーザー名");
        FIELD_LABELS.put("ownerDeptId", "担当部署ID");
        FIELD_LABELS.put("ownerDeptName", "担当部署名");
        FIELD_LABELS.put("stockId", "在庫ID");
        FIELD_LABELS.put("stockRecordId", "在庫履歴ID");
        FIELD_LABELS.put("permissionId", "権限ID");
        FIELD_LABELS.put("roleId", "ロールID");
        FIELD_LABELS.put("path", "パス");
        FIELD_LABELS.put("icon", "アイコン");
        FIELD_LABELS.put("component", "コンポーネント");
        FIELD_LABELS.put("operation", "操作種別");
        FIELD_LABELS.put("method", "HTTPメソッド");
        FIELD_LABELS.put("requestUrl", "リクエストURL");
        FIELD_LABELS.put("requestIp", "リクエストIP");
        FIELD_LABELS.put("requestParam", "リクエストパラメータ");
        FIELD_LABELS.put("responseData", "レスポンスデータ");
        FIELD_LABELS.put("errorMsg", "エラーメッセージ");
        FIELD_LABELS.put("costTime", "処理時間(ms)");
        FIELD_LABELS.put("loginTime", "ログイン時刻");
        FIELD_LABELS.put("loginIp", "ログインIP");
        FIELD_LABELS.put("token", "トークン");
        FIELD_LABELS.put("deleted", "削除フラグ");
        FIELD_LABELS.put("version", "バージョン");
        FIELD_LABELS.put("quantity", "数量");

        setType("status", FieldType.SELECT);
        setType("state", FieldType.SELECT);
        setType("isRead", FieldType.SELECT);
        setType("isHot", FieldType.SELECT);
        RELATION_FIELD_MODULE.keySet().forEach(k -> setType(k, FieldType.RELATION));

        putOptions("status", List.of(new Option("有効", "1"), new Option("無効", "0")));
        putOptions("stockOrder.orderType", List.of(
                new Option("入庫", "1"), new Option("出庫", "2"), new Option("調整", "3"),
                new Option("返品", "4"), new Option("移動", "5"), new Option("棚卸", "6")
        ));
        putOptions("stockOrder.sourceType", List.of(
                new Option("通常", "1"), new Option("棚卸", "2"), new Option("申請連携", "3"), new Option("手動", "4")
        ));
        putOptions("stockOrder.state", List.of(
                new Option("下書き", "0"), new Option("処理中", "1"), new Option("完了", "2"), new Option("取消", "3")
        ));
        putOptions("requestForm.state", List.of(
                new Option("下書き", "0"), new Option("処理中", "1"), new Option("完了", "2"), new Option("取消", "3")
        ));

        DEPENDENCY_RULES.put("goods", List.of(
                new DependencyRule("brandId", "seriesId", "series", "brandId", List.of("goodsId", "skuId")),
                new DependencyRule("seriesId", "goodsId", "goods", "seriesId", List.of("skuId")),
                new DependencyRule("categoryId", "goodsId", "goods", "categoryId", List.of("skuId")),
                new DependencyRule("goodsId", "skuId", "goodsSku", "goodsId", List.of())
        ));
        DEPENDENCY_RULES.put("stock", List.of(new DependencyRule("goodsId", "skuId", "goodsSku", "goodsId", List.of())));
        DEPENDENCY_RULES.put("requestItem", List.of(new DependencyRule("goodsId", "skuId", "goodsSku", "goodsId", List.of())));
        DEPENDENCY_RULES.put("stockOrderItem", List.of(new DependencyRule("goodsId", "skuId", "goodsSku", "goodsId", List.of())));
    }

    private static void setType(String field, FieldType type) {
        FIELD_TYPES.computeIfAbsent("_GLOBAL", k -> new HashMap<>()).put(field, type);
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
        ordered.add("id");
        ordered.addAll(queryFields(moduleKey));
        for (String k : keys) {
            ordered.add(k);
        }
        ordered.remove("__selected");
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
        String label = FIELD_LABELS.get(key);
        if (label != null && !label.isBlank()) {
            return label;
        }
        return "項目";
    }

    public static List<DependencyRule> dependencyRules(String moduleKey) {
        return DEPENDENCY_RULES.getOrDefault(moduleKey, List.of());
    }
}
