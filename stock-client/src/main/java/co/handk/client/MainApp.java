package co.handk.client;

import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import co.handk.client.view.LoginView;
import co.handk.client.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        ApiClient.setLoginTimeoutHandler(() -> {
            Session.clear();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("登录状态");
            alert.setHeaderText(null);
            alert.setContentText("login timeout");
            alert.showAndWait();
            showLogin();
        });
        showLogin();
    }

    public void showLogin() {
        LoginView view = new LoginView(this);
        primaryStage.setScene(new Scene(view.getView(), 300, 200));
        primaryStage.setTitle("登录");
        primaryStage.show();
    }

    public void showMain() {
        MainView view = new MainView(this);
        primaryStage.setScene(new Scene(view.getView(), 400, 300));
        primaryStage.setTitle("主页");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
