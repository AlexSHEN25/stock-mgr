package co.handk.client.view;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

public class LoginView {

    private VBox view;

    public LoginView(MainApp app) {
        view = new VBox(10);
        view.setPadding(new Insets(20));

        TextField username = new TextField();
        username.setPromptText("用户名");

        PasswordField password = new PasswordField();
        password.setPromptText("密码");

        Button loginBtn = new Button("登录");
        Label msg = new Label();

        loginBtn.setOnAction(e -> {
            try {
                String body = String.format(
                        "{\"username\":\"%s\",\"password\":\"%s\"}",
                        username.getText(), password.getText()
                );

                String res = ApiClient.post("/user/login", body);

                JSONObject json = new JSONObject(res);
                if (json.getInt("code") == 0) {
                    JSONObject data = json.getJSONObject("data");

                    String token = data.getString("token");
                    Long userId = data.getLong("userId");
                    String name = data.getString("username");

                    Session.set(token, userId, name);

                    app.showMain();
                } else {
                    msg.setText(json.getString("message"));
                }

            } catch (Exception ex) {
                msg.setText("请求失败：" + ex.getMessage());
            }
        });

        view.getChildren().addAll(username, password, loginBtn, msg);
    }

    public VBox getView() {
        return view;
    }
}
