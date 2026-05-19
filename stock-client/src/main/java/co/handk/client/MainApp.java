package co.handk.client;

import co.handk.client.controller.LoginController;
import co.handk.client.controller.MainController;
import co.handk.client.model.Session;
import co.handk.client.util.ApiClient;
import javafx.application.Application;
import javafx.application.Platform;
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
            alert.setTitle("登录失效");
            alert.setHeaderText(null);
            alert.setContentText("登录状态已失效，请重新登录");
            alert.showAndWait();
            showLogin();
        });
        showLogin();
    }

    public void showLogin() {
        try {
            System.out.println("[NAV] loading login.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 860, 620);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            LoginController controller = loader.getController();
            controller.setApp(this);

            primaryStage.setScene(scene);
            primaryStage.setTitle("库存系统登录");
            primaryStage.show();
            System.out.println("[NAV] login view shown");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("加载登录页面失败", e);
        }
    }

    public void showMain() {
        try {
            System.out.println("[NAV] loading main.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1360, 820);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            MainController controller = loader.getController();

            primaryStage.setScene(scene);
            primaryStage.setTitle("Stock Admin - JavaFX");
            primaryStage.show();
            System.out.println("[NAV] main view shown");

            Platform.runLater(() -> {
                try {
                    controller.setApp(this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("主界面初始化失败");
                    alert.setHeaderText(null);
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("加载主页面失败", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}