package com.example;

import com.repository.OrderDAO;
import com.service.UserService;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class LoginController {
    private static final String ALERT_TITLE = "Thông báo hệ thống";

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private Button btnTogglePassword;

    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
        txtPasswordVisible.setVisible(false);
        txtPasswordVisible.setManaged(false);
    }

    @FXML
    public void handleTogglePassword() {
        isPasswordVisible = !isPasswordVisible;
        txtPasswordVisible.setVisible(isPasswordVisible);
        txtPasswordVisible.setManaged(isPasswordVisible);
        txtPassword.setVisible(!isPasswordVisible);
        txtPassword.setManaged(!isPasswordVisible);
        btnTogglePassword.setText(isPasswordVisible ? "🙈" : "👁");
    }

    private App app;

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showWarning("Thiếu thông tin", "Vui lòng nhập tài khoản và mật khẩu.");
            return;
        }

        if (!UserService.getInstance().authenticate(username, password)) {
            showWarning("Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu.");
            return;
        }

        App.setCurrentUser(username);
        App.setCurrentRole(UserService.getInstance().getRole(username));

       
        double spent = UserService.getInstance().getTotalSpent(username);
        MembershipStore.loadForUser(spent);

        // Tải lịch sử đơn hàng của tài khoản từ DB
        List<OrderHistoryStore.Order> orders = new OrderDAO().loadByUser(username);
        OrderHistoryStore.loadFromDB(orders);

        try {
            app.showShopScene();
        } catch (Exception ex) {
            ex.printStackTrace();
            showWarning("Lỗi hệ thống", "Không thể quay lại cửa hàng.");
        }
    }

    @FXML
    public void handleBackToShop() {
        try {
            app.showShopScene();
        } catch (Exception ex) {
            showWarning("Lỗi hệ thống", "Không thể quay lại trang cửa hàng.");
        }
    }

    @FXML
    public void handleGoToRegister() {
        try {
            app.showRegisterScene();
        } catch (Exception ex) {
            showWarning("Lỗi hệ thống", "Không thể mở trang đăng ký.");
        }
    }

    @FXML
    public void handleForgotPassword() {
        try {
            app.showForgotPasswordScene();
        } catch (Exception ex) {
            showWarning("Lỗi hệ thống", "Không thể mở trang quên mật khẩu.");
        }
    }

    private void showWarning(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(ALERT_TITLE);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}