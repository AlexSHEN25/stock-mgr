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
            alert.setTitle("ログイン状態");
            alert.setHeaderText(null);
            alert.setContentText("ログインの有効期限が切れました。再ログインしてください。");
            alert.showAndWait();
            showLogin();
        });
        showLogin();
    }

    public void showLogin() {
        LoginView view = new LoginView(this);
        primaryStage.setScene(new Scene(view.getView(), 320, 220));
        primaryStage.setTitle("ログイン");
        primaryStage.show();
    }

    public void showMain() {
        MainView view = new MainView(this);
        primaryStage.setScene(new Scene(view.getView(), 1200, 760));
        primaryStage.setTitle("管理画面");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
