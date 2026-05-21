package co.handk.client.constant;

public final class AppConstants {

    private AppConstants() {
    }

    public static final class Module {
        public static final String USER = "user";
        public static final String GOODS = "goods";
        public static final String GOODS_SKU = "goodsSku";
        public static final String SERIES = "series";
        public static final String CATEGORY = "category";
        public static final String STOCK_ORDER = "stockOrder";
        public static final String STOCK_ORDER_ITEM = "stockOrderItem";
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

        private Field() {
        }
    }

    public static final class ApiPath {
        public static final String PAGE_SUFFIX = "/page";
        public static final String USER_PAGE = "/user/page";
        public static final String USER_LOGIN = "/user/login";
        public static final String USER_LOGOUT = "/user/logout";
        public static final String GOODS_SKU_PAGE = "/goodsSku/page";
        public static final String GOODS_PAGE = "/goods/page";
        public static final String SERIES_PAGE = "/series/page";
        public static final String REQUEST_FORM_DOWNLOAD_V1 = "/requestForm/download/";
        public static final String REQUEST_FORM_DOWNLOAD_V2_PREFIX = "/requestForm/";
        public static final String REQUEST_FORM_DOWNLOAD_V2_SUFFIX = "/download";

        private ApiPath() {
        }
    }
}

