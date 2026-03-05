package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    private static final String ALERT_TITLE = "Thông báo hệ thống";

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;

    private App app;

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void handleRegister() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showWarning("Thiếu thông tin", "Vui lòng nhập đầy đủ tất cả các trường.");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showWarning("Email không hợp lệ", "Vui lòng nhập đúng định dạng email.");
            return;
        }

        if (password.length() < 6) {
            showWarning("Mật khẩu quá ngắn", "Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showWarning("Mật khẩu không khớp", "Mật khẩu xác nhận không trùng khớp.");
            return;
        }

        if (UserStore.userExists(username)) {
            showWarning("Tài khoản đã tồn tại", "Tên đăng nhập \"" + username + "\" đã được sử dụng.");
            return;
        }

        UserStore.register(username, password, fullName, email);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(ALERT_TITLE);
        alert.setHeaderText("Đăng ký thành công");
        alert.setContentText("Tài khoản \"" + username + "\" đã được tạo. Vui lòng đăng nhập.");
        alert.showAndWait();

        handleGoToLogin();
    }

    @FXML
    public void handleGoToLogin() {
        try {
            app.showLoginScene();
        } catch (Exception ex) {
            showWarning("Lỗi hệ thống", "Không thể mở trang đăng nhập.");
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
