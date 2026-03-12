package com.example;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    private Stage primaryStage;

    /** Tên người dùng đang đăng nhập, null nghĩa là khách */
    private static String currentUser = null;   
    public static String getCurrentUser() { return currentUser; }
    public static void setCurrentUser(String user) { currentUser = user; }
    public static boolean isLoggedIn() { return currentUser != null; }
    public static void logout() { currentUser = null; }

    public static String getDisplayName() {
        if (currentUser == null) return "Guest";
        return UserStore.getFullName(currentUser);
    }

    /** Hỗ trợ – chuyển tài nguyên CSS thành chuỗi URL dạng external-form. */
    private static String css(String name) {
        return App.class.getResource("/com/example/" + name).toExternalForm();
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        setApplicationIcon(primaryStage);
        showShopScene();
    }

    private static void setApplicationIcon(Stage stage) {
        URL iconUrl = App.class.getResource("/com/example/photo/download.png");
        if (iconUrl != null) {
            stage.getIcons().setAll(new Image(iconUrl.toExternalForm()));
        }
    }

    public void showShopScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/shop.fxml"));
        Parent root = loader.load();

        ShopController controller = loader.getController();
        controller.setApp(this);

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().addAll(css("common.css"), css("shop.css"));
        primaryStage.setTitle("Flower Shop");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showCheckoutScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/checkout.fxml"));
        Parent root = loader.load();

        CheckoutController controller = loader.getController();
        controller.setApp(this);

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().addAll(css("common.css"), css("checkout.css"));
        primaryStage.setTitle("Thanh toán");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showHistoryScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/history.fxml"));
        Parent root = loader.load();

        HistoryController controller = loader.getController();
        controller.setApp(this);

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().addAll(css("common.css"), css("shop.css"), css("history.css"));
        primaryStage.setTitle("Lịch sử mua hàng");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showLoginScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setApp(this);

        Scene scene = new Scene(root, 580, 520);
        scene.getStylesheets().addAll(css("common.css"), css("auth.css"));
        primaryStage.setTitle("Flower CRM - Đăng nhập");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showRegisterScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/register.fxml"));
        Parent root = loader.load();

        RegisterController controller = loader.getController();
        controller.setApp(this);

        Scene scene = new Scene(root, 580, 620);
        scene.getStylesheets().addAll(css("common.css"), css("auth.css"));
        primaryStage.setTitle("Flower CRM - Đăng ký");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showForgotPasswordScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/forgot_password.fxml"));
        Parent root = loader.load();

        ForgotPasswordController controller = loader.getController();
        controller.setApp(this);

        Scene scene = new Scene(root, 580, 560);
        scene.getStylesheets().addAll(css("common.css"), css("auth.css"));
        primaryStage.setTitle("Flower CRM - Quên mật khẩu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showProfileScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/profile.fxml"));
        Parent root = loader.load();

        ProfileController controller = loader.getController();
        controller.setApp(this);

        Scene scene = new Scene(root, 580, 660);
        scene.getStylesheets().addAll(css("common.css"), css("auth.css"), css("profile.css"));
        primaryStage.setTitle("Hồ sơ cá nhân");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showCrudScene(String username) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/crud.fxml"));
        Parent root = loader.load();

        CrudController controller = loader.getController();
        controller.setApp(this);
        controller.setCurrentUser(username);

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().addAll(css("common.css"), css("crud-page.css"));
        primaryStage.setTitle("Flower Management Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
