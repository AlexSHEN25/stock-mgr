package co.handk.client;

import co.handk.client.controller.LoginController;
import co.handk.client.controller.MainController;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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
            alert.setTitle("登录超时");
            alert.setHeaderText(null);
            alert.setContentText("登录已过期，请重新登录");
            alert.showAndWait();
            showLogin();
        });
        showLogin();
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 360, 240);
            LoginController controller = loader.getController();
            controller.setApp(this);

            primaryStage.setScene(scene);
            primaryStage.setTitle("库存系统登录");
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException("加载登录页面失败", e);
        }
    }

    public void showMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1180, 720);
            MainController controller = loader.getController();
            controller.setApp(this);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Stock Admin - JavaFX");
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException("加载主页失败", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
