package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    private static final String ALERT_TITLE = "Thông báo hệ thống";

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;

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

        if (!UserStore.authenticate(username, password)) {
            showWarning("Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu.");
            return;
        }

        App.setCurrentUser(username);

        try {
            app.showCrudScene(username);
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