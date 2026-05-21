package co.handk.client.service;

import co.handk.client.constant.UiText;
import org.json.JSONObject;

public class UiFeedbackService {

    public boolean isSuccess(JSONObject json) {
        int code = json.optInt("code", -1);
        return code == 200 || code == 0;
    }

    public String resolveMessage(JSONObject json, String fallback) {
        return json.optString("message", fallback);
    }

    public String saveSuccess(boolean editMode) {
        return editMode ? UiText.MSG_SAVE_SUCCESS_UPDATE : UiText.MSG_SAVE_SUCCESS_CREATE;
    }

    public String saveFailed(Exception ex) {
        return UiText.MSG_SAVE_FAILED + ex.getMessage();
    }

    public String loadFailed(Exception ex) {
        return UiText.MSG_LOAD_FAILED + ": " + ex.getMessage();
    }

    public String deleteFailed(Exception ex) {
        return UiText.MSG_DELETE_FAILED + ": " + ex.getMessage();
    }

    public String logoutFailed(Exception ex) {
        return UiText.MSG_LOGOUT_FAILED + ": " + ex.getMessage();
    }

    public String formOpenFailed(Exception ex) {
        return UiText.MSG_FORM_OPEN_FAILED + ex.getMessage();
    }

    public String downloadFailed(Exception ex) {
        return UiText.MSG_DOWNLOAD_FAIL + ex.getMessage();
    }
}

