package co.handk.common.constant;

public final class StockBizConstant {

    private StockBizConstant() {
    }

    public static final int ORDER_TYPE_INBOUND = 1;
    public static final int ORDER_TYPE_OUTBOUND = 2;

    public static final int SOURCE_TYPE_REQUEST = 3;
    public static final int SOURCE_TYPE_MANUAL = 4;

    public static final int ORDER_STATE_DRAFT = 0;
    public static final int ORDER_STATE_APPROVING = 1;
    public static final int ORDER_STATE_FINISHED = 2;
    public static final int ORDER_STATE_CANCELED = 3;

    public static final int INBOUND_SCENE_SELF = 1;
    public static final int INBOUND_SCENE_RESALE = 2;
}
