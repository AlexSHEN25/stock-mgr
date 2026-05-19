package co.handk.client.controller;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.util.prefs.Preferences;

public class LoginController {

    private static final String PREF_NODE = "stock-client";
    private static final String PREF_REMEMBER = "remember";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberCheckBox;
    @FXML
    private Label messageLabel;

    private final Preferences prefs = Preferences.userRoot().node(PREF_NODE);
    private MainApp app;

    public void setApp(MainApp app) {
        this.app = app;
        loadRememberedCredentials();
    }

    @FXML
    private void onLogin() {
        try {
            System.out.println("[LOGIN] submit start, username=" + usernameField.getText());
            JSONObject loginDto = new JSONObject();
            loginDto.put("username", usernameField.getText());
            loginDto.put("password", passwordField.getText());

            String res = ApiClient.post("/user/login", loginDto.toString());
            System.out.println("[LOGIN] raw response=" + res);
            JSONObject json = new JSONObject(res);

            if (isLoginSuccess(json)) {
                JSONObject data = extractLoginData(json);
                String token = data.optString("token", "");
                if (token.isBlank()) {
                    messageLabel.setText("Login failed: missing token");
                    return;
                }

                saveRememberedCredentials();
                Session.set(token, data.optLong("userId", 0L), data.optString("username", usernameField.getText()));
                ApiClient.resetLoginTimeoutHandled();
                System.out.println("[LOGIN] parsed success, userId=" + Session.getUserId() + ", username=" + Session.getUsername());
                System.out.println("[LOGIN] calling app.showMain()");
                app.showMain();
                System.out.println("[LOGIN] app.showMain() returned");
            } else {
                String msg = json.optString("message", json.optString("msg", "Login failed"));
                System.out.println("[LOGIN] login failed message=" + msg);
                messageLabel.setText(msg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("Request failed: " + ex.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("逋ｻ蠖募､ｱ雍･");
            alert.setHeaderText(null);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void loadRememberedCredentials() {
        boolean remember = prefs.getBoolean(PREF_REMEMBER, false);
        rememberCheckBox.setSelected(remember);
        if (!remember) {
            return;
        }
        usernameField.setText(prefs.get(PREF_USERNAME, ""));
        passwordField.setText(prefs.get(PREF_PASSWORD, ""));
    }

    private void saveRememberedCredentials() {
        if (rememberCheckBox.isSelected()) {
            prefs.putBoolean(PREF_REMEMBER, true);
            prefs.put(PREF_USERNAME, usernameField.getText() == null ? "" : usernameField.getText());
            prefs.put(PREF_PASSWORD, passwordField.getText() == null ? "" : passwordField.getText());
        } else {
            prefs.putBoolean(PREF_REMEMBER, false);
            prefs.remove(PREF_USERNAME);
            prefs.remove(PREF_PASSWORD);
        }
    }

    private boolean isLoginSuccess(JSONObject json) {
        if (json.has("token")) {
            return true;
        }
        if (json.has("data") && json.optJSONObject("data") != null && json.optJSONObject("data").has("token")) {
            return true;
        }
        if (json.has("code")) {
            int code = json.optInt("code", -1);
            System.out.println("[LOGIN] parsed code=" + code);
            return code == 200 || code == 0;
        }
        return false;
    }

    private JSONObject extractLoginData(JSONObject json) {
        if (json.has("data") && json.optJSONObject("data") != null) {
            return json.getJSONObject("data");
        }
        return json;
    }
}
