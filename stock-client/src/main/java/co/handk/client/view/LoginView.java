package co.handk.client.view;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

public class LoginView {

    private final VBox view;

    public LoginView(MainApp app) {
        view = new VBox(10);
        view.setPadding(new Insets(20));

        TextField username = new TextField();
        username.setPromptText("ユーザー名");

        PasswordField password = new PasswordField();
        password.setPromptText("パスワード");

        Button loginBtn = new Button("ログイン");
        Label msg = new Label();

        loginBtn.setOnAction(e -> {
            try {
                String body = String.format(
                        "{\"username\":\"%s\",\"password\":\"%s\"}",
                        username.getText(), password.getText()
                );

                String res = ApiClient.post("/user/login", body);
                JSONObject json = new JSONObject(res);

                if (json.optInt("code", -1) == 0) {
                    JSONObject data = json.getJSONObject("data");
                    Session.set(
                            data.getString("token"),
                            data.getLong("userId"),
                            data.getString("username")
                    );
                    app.showMain();
                } else {
                    msg.setText(json.optString("message", "ログインに失敗しました"));
                }
            } catch (Exception ex) {
                msg.setText("ログインエラー: " + ex.getMessage());
            }
        });

        view.getChildren().addAll(username, password, loginBtn, msg);
    }

    public VBox getView() {
        return view;
    }
}
