package co.handk.client.constant;

import java.util.Locale;
import java.util.ResourceBundle;

public final class UiText {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("i18n.ui", Locale.JAPAN);

    private UiText() {
    }

    private static String t(String key) {
        return BUNDLE.getString(key);
    }

    public static final String LABEL_LOGIN_USER_PREFIX = t("msg.loginUserPrefix");
    public static final String MSG_FIRST_GUIDE = t("msg.firstGuide");
    public static final String MSG_EMPTY_RESULT = t("msg.emptyResult");

    public static final String ACTION_CREATE = t("action.create");
    public static final String ACTION_EDIT = t("action.edit");
    public static final String ACTION_ORDER_DETAIL = t("action.orderDetail");
    public static final String ACTION_REQUEST_DETAIL = t("action.requestDetail");
    public static final String ACTION_DOWNLOAD = t("action.download");
    public static final String ACTION_DOWNLOAD_EXCEL = t("action.downloadExcel");
    public static final String ACTION_DOWNLOAD_PDF = t("action.downloadPdf");

    public static final String TITLE_CONFIRM_BATCH_DELETE = t("title.confirmBatchDelete");
    public static final String TITLE_CONFIRM_DELETE = t("title.confirmDelete");
    public static final String TITLE_CONFIRM_LOGOUT = t("title.confirmLogout");
    public static final String TITLE_SAVE_FILE = t("title.saveFile");
    public static final String TITLE_LOGIN_FAILED = t("title.loginFailed");

    public static final String MSG_CONFIRM_BATCH_DELETE = t("msg.confirmBatchDelete");
    public static final String MSG_CONFIRM_DELETE = t("msg.confirmDelete");
    public static final String MSG_CONFIRM_LOGOUT = t("msg.confirmLogout");
    public static final String MSG_BATCH_DELETE_CANCELLED = t("msg.batchDeleteCancelled");
    public static final String MSG_DELETE_CANCELLED = t("msg.deleteCancelled");
    public static final String MSG_SELECT_ROW_FIRST = t("msg.selectRowFirst");
    public static final String MSG_INLINE_EDIT_STARTED = t("msg.inlineEditStarted");
    public static final String MSG_INLINE_EDIT_NONE = t("msg.inlineEditNone");
    public static final String MSG_INLINE_UPDATE_SUCCESS = t("msg.inlineUpdateSuccess");
    public static final String MSG_INLINE_UPDATE_FAILED = t("msg.inlineUpdateFailed");
    public static final String MSG_INLINE_CANCELLED = t("msg.inlineCancelled");
    public static final String MSG_BATCH_DELETE_CHECK = t("msg.batchDeleteCheck");
    public static final String MSG_BATCH_DELETE_DONE = t("msg.batchDeleteDone");
    public static final String MSG_DELETE_ID_REQUIRED = t("msg.deleteIdRequired");
    public static final String MSG_DELETE_SUCCESS = t("msg.deleteSuccess");
    public static final String MSG_DELETE_FAILED = t("msg.deleteFailed");
    public static final String MSG_LOGOUT_FAILED = t("msg.logoutFailed");
    public static final String MSG_FORM_OPEN_FAILED = t("msg.formOpenFailed");
    public static final String MSG_SAVE_FAILED = t("msg.saveFailed");
    public static final String MSG_UPDATE_FAILED = t("msg.updateFailed");
    public static final String MSG_SAVE_SUCCESS_CREATE = t("msg.saveSuccessCreate");
    public static final String MSG_SAVE_SUCCESS_UPDATE = t("msg.saveSuccessUpdate");
    public static final String MSG_LOAD_FAILED = t("msg.loadFailed");
    public static final String MSG_LOAD_SUCCESS = t("msg.loadSuccess");
    public static final String MSG_RESPONSE_DATA_EMPTY = t("msg.responseDataEmpty");
    public static final String MSG_RELATION_ID_NOT_FOUND = t("msg.relationIdNotFound");
    public static final String MSG_DOWNLOAD_DONE = t("msg.downloadDone");
    public static final String MSG_DOWNLOAD_FAIL = t("msg.downloadFail");
    public static final String MSG_EDIT_ID_REQUIRED = t("msg.editIdRequired");
    public static final String MSG_JSON_PARSE_FAIL = t("msg.jsonParseFail");
    public static final String MSG_REQUIRED_SUFFIX = t("msg.requiredSuffix");
    public static final String MSG_LOGIN_TOKEN_EMPTY = t("msg.loginTokenEmpty");
    public static final String MSG_REQUEST_FAILED_PREFIX = t("msg.requestFailedPrefix");
    public static final String MSG_LOGIN_FAILED_DEFAULT = t("msg.loginFailedDefault");

    public static final String PAGE_INFO_FORMAT = t("format.pageInfo");
    public static final String DOWNLOAD_FILENAME_PATTERN = t("format.downloadFilename");
    public static final String DOWNLOAD_PDF_FILENAME_PATTERN = t("format.downloadPdfFilename");
    public static final String DELETE_ID_PATTERN = t("format.deleteId");
    public static final String RELATION_FALLBACK_PATTERN = t("format.relationFallback");
    public static final String FIELD_ID = t("field.id");

    public static String byKey(String key) {
        return t(key);
    }
}
