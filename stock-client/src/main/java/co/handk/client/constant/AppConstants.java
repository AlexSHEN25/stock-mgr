package co.handk.client.constant;

public final class AppConstants {

    private AppConstants() {
    }

    public static final class Module {
        public static final String USER = "user";
        public static final String MESSAGE = "message";
        public static final String GOODS = "goods";
        public static final String GOODS_SKU = "goodsSku";
        public static final String SERIES = "series";
        public static final String CATEGORY = "category";
        public static final String STOCK_ORDER = "stockOrder";
        public static final String STOCK_ORDER_ITEM = "stockOrderItem";
        public static final String STOCK = "stock";
        public static final String SELF_STOCK = "selfStock";
        public static final String HANDLE_STOCK = "handleStock";
        public static final String REQUEST_FORM = "requestForm";
        public static final String REQUEST_ITEM = "requestItem";

        private Module() {
        }
    }

    public static final class Field {
        public static final String ID = "id";
        public static final String SKU_ID = "skuId";
        public static final String GOODS_ID = "goodsId";
        public static final String CATEGORY_ID = "categoryId";
        public static final String SERIES_ID = "seriesId";
        public static final String BRAND_ID = "brandId";
        public static final String ORDER_ID = "orderId";
        public static final String REQUEST_ID = "requestId";
        public static final String STATUS = "status";
        public static final String STATE = "state";
        public static final String IS_READ = "isRead";
        public static final String CREATE_TIME = "createTime";
        public static final String UPDATE_TIME = "updateTime";

        private Field() {
        }
    }

    public static final class ApiPath {
        public static final String PAGE_SUFFIX = "/page";
        public static final String USER_PAGE = "/user/page";
        public static final String USER_LOGIN = "/user/login";
        public static final String USER_LOGOUT = "/user/logout";
        public static final String USER_PASSWORD_PREFIX = "/user/";
        public static final String USER_PASSWORD_SUFFIX = "/password";
        public static final String GOODS_SKU_PAGE = "/goodsSku/page";
        public static final String GOODS_PAGE = "/goods/page";
        public static final String SERIES_PAGE = "/series/page";
        public static final String REQUEST_FORM_DOWNLOAD_V1 = "/requestForm/download/";
        public static final String REQUEST_FORM_DOWNLOAD_V2_PREFIX = "/requestForm/";
        public static final String REQUEST_FORM_DOWNLOAD_V2_SUFFIX = "/download";
        public static final String REQUEST_FORM_PDF_V2_SUFFIX = "/pdf";
        public static final String REQUEST_FORM_CANDIDATE_ITEMS_SUFFIX = "/candidateItems";
        public static final String REQUEST_FORM_MATCH_ITEMS = "/requestForm/items/match";
        public static final String MESSAGE_READ_PREFIX = "/message/read/";
        public static final String MESSAGE_READ_ALL = "/message/read-all";
        public static final String STOCK_INBOUND = "/stock/inbound";

        private ApiPath() {
        }
    }
}
