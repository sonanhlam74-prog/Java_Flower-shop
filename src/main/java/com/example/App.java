package com.example;

import java.net.InetAddress;
import java.net.URL;
import java.util.List;

import com.Connect.DatabaseSetup;
import com.repository.GuestSpendingDAO;
import com.repository.OrderDAO;
import com.service.UserService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class App extends Application {
    private Stage primaryStage;

    /** Tên người dùng đang đăng nhập, null nghĩa là khách */
    private static String currentUser = null;
    public static String getCurrentUser() { return currentUser; }
    public static void setCurrentUser(String user) { currentUser = user; }
    public static boolean isLoggedIn() { return currentUser != null; }

    private static String currentRole = "customer";
    public static boolean isAdmin() { return "admin".equals(currentRole); }
    public static void setCurrentRole(String role) { currentRole = role != null ? role : "customer"; }

    public static void logout() { currentUser = null; currentRole = "customer"; }

    /** Địa chỉ IP của máy — dùng làm định danh khách vãng lai trong DB */
    private static String guestIp = null;
    public static String getGuestIp() {
        if (guestIp == null) {
            try {
                guestIp = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                guestIp = "127.0.0.1";
            }
        }
        return guestIp;
    }

    public static String getDisplayName() {
        if (currentUser == null) return "Guest";
        return UserService.getInstance().getFullName(currentUser);
    }

    /** Hỗ trợ – chuyển tài nguyên CSS thành chuỗi URL dạng external-form. */
    private static String css(String name) {
        return App.class.getResource("/com/example/" + name).toExternalForm();
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Hiển thị Splash screen ngay, sau đó làm khởi tạo nặng ở background để UI không block
        setApplicationIcon(primaryStage);
        showSplashScreen();

        new Thread(() -> {
            try {
                DatabaseSetup.initialize();

                double guestSpent = new GuestSpendingDAO().getTotalSpent(getGuestIp());
                MembershipStore.loadForUser(guestSpent);

                List<OrderHistoryStore.Order> guestOrders = new OrderDAO().loadByGuestIp(getGuestIp());
                OrderHistoryStore.loadFromDB(guestOrders);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, "AppInitThread").start();
    }

    private static void setApplicationIcon(Stage stage) {
        URL iconUrl = App.class.getResource("/com/example/photo/download.png");
        if (iconUrl != null) {
            stage.getIcons().setAll(new Image(iconUrl.toExternalForm()));
        }
    }

    public void showShopScene() {
        try {
            System.out.println("[App] showShopScene: loading shop.fxml");
            URL fxmlUrl = App.class.getResource("/com/example/shop.fxml");
            if (fxmlUrl == null) {
                throw new IllegalStateException("Không tìm thấy tài nguyên shop.fxml trong classpath");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            ShopController controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("ShopController không được khởi tạo từ shop.fxml");
            }
            controller.setApp(this);

            Scene scene = new Scene(root, 1200, 760);
            scene.getStylesheets().addAll(css("common.css"), css("shop.css"));
            primaryStage.setTitle("Flower Shop");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Không thể tải giao diện cửa hàng", ex);

            // Fallback: hiển thị thông báo đơn giản để ứng dụng không bị treo
            javafx.application.Platform.runLater(() -> {
                try {
                    Label errorLabel = new Label("Không thể tải giao diện cửa hàng. Vui lòng mở lại ứng dụng và kiểm tra log.");
                    errorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: center;");
                    StackPane fallback = new StackPane(errorLabel);
                    fallback.setStyle("-fx-background-color: #d32f2f;");
                    Scene fallbackScene = new Scene(fallback, 800, 500);
                    primaryStage.setScene(fallbackScene);
                    primaryStage.setTitle("Lỗi Flower Shop");
                    primaryStage.show();
                } catch (Exception ignored) {
                }
            });
        }
    }

    private void showError(String message, Exception ex) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Lỗi hệ thống");
            alert.setHeaderText(message);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        });
    }

    public void showSplashScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/SplashScreen.fxml"));
        Parent root = loader.load();

        SplashController controller = loader.getController();
        controller.setApp(this);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Flower Shop - Loading");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
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
    
    //loading màn hình chờ của dashboard (CRUD) sau khi đăng nhập thành công
    public void showLoadingBeforeDashboard(String username) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/SplashScreen.fxml"));
        Parent root = loader.load();

        SplashController controller = loader.getController();
        controller.setOnComplete(() -> {
            try {
                showCrudScene(username);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        controller.setUsername(username);
        controller.setApp(this);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(css("common.css"));
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
