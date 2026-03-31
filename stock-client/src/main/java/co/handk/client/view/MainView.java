package co.handk.client.view;

import co.handk.client.MainApp;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

public class MainView {

    private VBox view;

    public MainView(MainApp app) {
        view = new VBox(10);
        view.setPadding(new Insets(20));

        Label welcome = new Label("欢迎：" + Session.getUsername());

        Button logoutBtn = new Button("登出");
        Label msg = new Label();

        logoutBtn.setOnAction(e -> {
            try {
                String res = ApiClient.post("/user/logout", "{}");

                JSONObject json = new JSONObject(res);
                if (json.getInt("code") == 0) {
                    Session.clear();
                    app.showLogin();
                } else {
                    msg.setText(json.getString("message"));
                }

            } catch (Exception ex) {
                msg.setText("登出失败：" + ex.getMessage());
            }
        });

        view.getChildren().addAll(welcome, logoutBtn, msg);
    }

    public VBox getView() {
        return view;
    }
}
