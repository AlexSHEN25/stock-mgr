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
            alert.setTitle("ログイン失効");
            alert.setHeaderText(null);
            alert.setContentText("ログイン状態が失効しました。再ログインしてください。");
            alert.showAndWait();
            showLogin();
        });
        showLogin();
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 860, 620);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            LoginController controller = loader.getController();
            controller.setApp(this);

            primaryStage.setScene(scene);
            primaryStage.setTitle("在庫管理ログイン");
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException("ログイン画面の読み込みに失敗しました。", e);
        }
    }

    public void showMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1360, 820);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            MainController controller = loader.getController();

            primaryStage.setScene(scene);
            primaryStage.setTitle("Stock Admin - JavaFX");
            primaryStage.show();

            Platform.runLater(() -> {
                try {
                    controller.setApp(this);
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("メイン画面初期化エラー");
                    alert.setHeaderText(null);
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("メイン画面の読み込みに失敗しました。", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}