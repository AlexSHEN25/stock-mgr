package co.handk.common.constant;

public final class StockBizConstant {

    private StockBizConstant() {
    }

    public static final int ORDER_TYPE_INBOUND = 1;
    public static final int ORDER_TYPE_OUTBOUND = 2;
    public static final int ORDER_TYPE_ADJUST = 3;
    public static final int ORDER_TYPE_STOCKTAKE = 4;
    public static final int ORDER_TYPE_TRANSFER = 5;
    public static final int ORDER_TYPE_RETURN = 6;

    public static final int SOURCE_TYPE_ORDER = 1;
    public static final int SOURCE_TYPE_RETURN = 2;
    public static final int SOURCE_TYPE_REQUEST = 3;
    public static final int SOURCE_TYPE_MANUAL = 4;

    public static final int ORDER_STATE_DRAFT = 0;
    public static final int ORDER_STATE_APPROVING = 1;
    public static final int ORDER_STATE_FINISHED = 2;
    public static final int ORDER_STATE_CANCELED = 3;

    public static final int INBOUND_SCENE_SELF = 1;
    public static final int INBOUND_SCENE_RESALE = 2;

    public static final String OUTBOUND_MODE_CUSTOMER = "CUSTOMER";
    public static final String OUTBOUND_MODE_GROUP_ALLOCATE = "GROUP_ALLOCATE";
    public static final String OUTBOUND_MODE_GROUP_CUSTOMER = "GROUP_CUSTOMER";

    public static final int BATCH_STATE_ACTIVE = 0;
    public static final int BATCH_STATE_EXHAUSTED = 1;
    public static final int BATCH_STATE_EXPIRED = 2;

    public static final String RESERVATION_SCOPE_SELF = "SELF";
    public static final String RESERVATION_SCOPE_GROUP = "GROUP";

    public static final int RESERVATION_STATE_LOCKED = 1;
    public static final int RESERVATION_STATE_CONFIRMED = 2;
    public static final int RESERVATION_STATE_RELEASED = 3;
    public static final int RESERVATION_STATE_EXPIRED = 4;

    public static final int REQUEST_STATE_DRAFT = 0;
    public static final int REQUEST_STATE_SUBMITTED = 1;
    public static final int REQUEST_STATE_FINISHED = 2;
    public static final int REQUEST_STATE_REJECTED = 3;
    public static final int REQUEST_STATE_REJECTED_ALT = 4;
    public static final int REQUEST_STATE_CANCELED = 5;

    public static final int REQUEST_STATE_CREATED = REQUEST_STATE_DRAFT;
    public static final int REQUEST_STATE_REINBOUND_APPLIED = REQUEST_STATE_FINISHED;

    public static final int REQUEST_ITEM_STATE_REMOVED = 0;
    public static final int REQUEST_ITEM_STATE_ADDED = 1;
}
