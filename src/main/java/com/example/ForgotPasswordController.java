package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ForgotPasswordController {
    private static final String ALERT_TITLE = "Thông báo hệ thống";

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblNewPassSection;
    @FXML private Label lblConfirmSection;
    @FXML private Label lblStatus;
    @FXML private Button btnAction;

    private App app;
    private boolean verified = false;

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void handleVerify() {
        if (verified) {
            handleResetPassword();
            return;
        }

        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            lblStatus.setText("Vui lòng nhập tài khoản và email.");
            lblStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (!UserStore.verifyEmail(username, email)) {
            lblStatus.setText("Tài khoản hoặc email không đúng.");
            lblStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        // Xác minh thành công — hiển thị ô nhập mật khẩu
        verified = true;
        lblStatus.setText("Xác minh thành công! Nhập mật khẩu mới.");
        lblStatus.setStyle("-fx-text-fill: #16a34a;");

        txtUsername.setEditable(false);
        txtUsername.setOpacity(0.6);
        txtEmail.setEditable(false);
        txtEmail.setOpacity(0.6);

        lblNewPassSection.setVisible(true);
        lblNewPassSection.setManaged(true);
        txtNewPassword.setVisible(true);
        txtNewPassword.setManaged(true);
        lblConfirmSection.setVisible(true);
        lblConfirmSection.setManaged(true);
        txtConfirmPassword.setVisible(true);
        txtConfirmPassword.setManaged(true);

        btnAction.setText("🔒 Đặt lại mật khẩu");
    }

    @FXML
    public void handleResetPassword() {
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (newPassword.isEmpty()) {
            lblStatus.setText("Vui lòng nhập mật khẩu mới.");
            lblStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (newPassword.length() < 6) {
            lblStatus.setText("Mật khẩu phải có ít nhất 6 ký tự.");
            lblStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            lblStatus.setText("Mật khẩu xác nhận không khớp.");
            lblStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        String username = txtUsername.getText().trim();
        UserStore.changePassword(username, newPassword);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(ALERT_TITLE);
        alert.setHeaderText("Đặt lại mật khẩu thành công");
        alert.setContentText("Mật khẩu mới đã được cập nhật. Vui lòng đăng nhập lại.");
        alert.showAndWait();

        handleGoToLogin();
    }

    @FXML
    public void handleGoToLogin() {
        try {
            app.showLoginScene();
        } catch (Exception ex) {
            lblStatus.setText("Không thể mở trang đăng nhập.");
            lblStatus.setStyle("-fx-text-fill: #ef4444;");
        }
    }
}
