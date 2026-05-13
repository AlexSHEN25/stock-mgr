package co.handk.client.controller;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.json.JSONObject;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private MainApp app;

    public void setApp(MainApp app) {
        this.app = app;
    }

    @FXML
    private void onLogin() {
        try {
            String body = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    usernameField.getText(), passwordField.getText()
            );
            String res = ApiClient.post("/user/login", body);
            JSONObject json = new JSONObject(res);

            if (json.optInt("code") == 0) {
                JSONObject data = json.getJSONObject("data");
                Session.set(data.getString("token"), data.getLong("userId"), data.getString("username"));
                app.showMain();
            } else {
                messageLabel.setText(json.optString("message", "登录失败"));
            }
        } catch (Exception ex) {
            messageLabel.setText("请求失败: " + ex.getMessage());
        }
    }
}
