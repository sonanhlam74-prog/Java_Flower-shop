package com.example;

import com.service.UserService;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

public class ProfileController {
    @FXML private Label lblUsername;
    @FXML private Label lblEmail;
    @FXML private Label lblRole;
    @FXML private TextField txtFullName;
    @FXML private Label lblNameStatus;
    @FXML private Label lblTierIcon;
    @FXML private Label lblTierName;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotalSpent;
    @FXML private Label lblNextTier;
    @FXML private Label lblAmountToNext;
    @FXML private StackPane avatarPane;
    @FXML private ImageView imgProfileAvatar;
    @FXML private Label lblAvatarInitial;
    @FXML private Label lblAvatarStatus;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblPasswordStatus;

    private App app;

    public void setApp(App app) {
        this.app = app;
        loadProfile();
    }

    private void loadProfile() {
        String username = App.getCurrentUser();
        if (username == null) {
            lblUsername.setText("Khách (Guest)");
            lblEmail.setText("—");
            lblRole.setText("Khách");
            txtFullName.setText("");
            txtFullName.setDisable(true);
            txtCurrentPassword.setDisable(true);
            txtNewPassword.setDisable(true);
            txtConfirmPassword.setDisable(true);
            lblPasswordStatus.setText("Vui lòng đăng nhập để đổi mật khẩu.");
            lblPasswordStatus.setStyle("-fx-text-fill: #ef4444;");
            showAvatarInitial("G");
        } else {
            lblUsername.setText(username);
            lblEmail.setText(UserService.getInstance().getEmail(username));
            lblRole.setText(UserService.getInstance().getRole(username));
            txtFullName.setText(UserService.getInstance().getFullName(username));
            txtFullName.setDisable(false);
            txtCurrentPassword.setDisable(false);
            txtNewPassword.setDisable(false);
            txtConfirmPassword.setDisable(false);
            lblPasswordStatus.setText("");
            loadAvatarImage(username);
        }
        loadMembership();
    }

    private void loadAvatarImage(String username) {
        String path = UserService.getInstance().getAvatarPath(username);
        if (path != null && !path.isBlank()) {
            try {
                File f = new File(path);
                if (f.exists()) {
                    Image img = new Image(f.toURI().toString(), 100, 100, false, true);
                    imgProfileAvatar.setImage(img);
                    applyCircleClip(imgProfileAvatar, 50);
                    imgProfileAvatar.setVisible(true);
                    imgProfileAvatar.setManaged(true);
                    lblAvatarInitial.setVisible(false);
                    lblAvatarInitial.setManaged(false);
                    return;
                }
            } catch (Exception ignored) { }
        }
        // Fallback: show initial letter
        String name = UserService.getInstance().getFullName(username);
        showAvatarInitial(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
    }

    private void showAvatarInitial(String initial) {
        lblAvatarInitial.setText(initial);
        lblAvatarInitial.setVisible(true);
        lblAvatarInitial.setManaged(true);
        imgProfileAvatar.setVisible(false);
        imgProfileAvatar.setManaged(false);
    }

    private void applyCircleClip(ImageView iv, double radius) {
        Circle clip = new Circle(radius, radius, radius);
        iv.setClip(clip);
    }

    private void loadMembership() {
        MembershipStore.Tier tier = MembershipStore.getCurrentTier();

        String icon = switch (tier) {
            case BRONZE  -> "🥉";
            case SILVER  -> "🥈";
            case GOLD    -> "🥇";
            case DIAMOND -> "💎";
            case VIP     -> "👑";
        };
        lblTierIcon.setText(icon);
        lblTierName.setText(tier.getDisplayName());
        lblDiscount.setText("Giảm giá: " + (int)(tier.getDiscountRate() * 100) + "%");
        lblTotalSpent.setText(String.format("%,.0f₫", MembershipStore.getTotalSpent()));

        MembershipStore.Tier next = MembershipStore.getNextTier();
        if (next != null) {
            lblNextTier.setText(next.getDisplayName());
            lblAmountToNext.setText(String.format("%,.0f₫", MembershipStore.getAmountToNextTier()));
        } else {
            lblNextTier.setText("Đã đạt tối đa");
            lblAmountToNext.setText("—");
        }
    }

    @FXML
    public void handleChangeAvatar() {
        String username = App.getCurrentUser();
        if (username == null) {
            lblAvatarStatus.setText("Vui lòng đăng nhập để thay đổi ảnh.");
            lblAvatarStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh đại diện");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        File chosen = chooser.showOpenDialog(avatarPane.getScene().getWindow());
        if (chosen == null) return;

        String path = chosen.getAbsolutePath();
        try {
            Image img = new Image(chosen.toURI().toString(), 100, 100, false, true);
            imgProfileAvatar.setImage(img);
            applyCircleClip(imgProfileAvatar, 50);
            imgProfileAvatar.setVisible(true);
            imgProfileAvatar.setManaged(true);
            lblAvatarInitial.setVisible(false);
            lblAvatarInitial.setManaged(false);

            UserService.getInstance().updateAvatarPath(username, path);
            lblAvatarStatus.setText("✅ Đã cập nhật ảnh đại diện!");
            lblAvatarStatus.setStyle("-fx-text-fill: #10b981;");
        } catch (Exception ex) {
            lblAvatarStatus.setText("❌ Không thể tải ảnh: " + ex.getMessage());
            lblAvatarStatus.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    public void handleSaveName() {
        String username = App.getCurrentUser();
        if (username == null) {
            lblNameStatus.setText("Vui lòng đăng nhập để chỉnh sửa.");
            lblNameStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        String newName = txtFullName.getText().trim();
        if (newName.isEmpty()) {
            lblNameStatus.setText("Tên không được để trống.");
            lblNameStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        UserService.getInstance().updateFullName(username, newName);
        lblNameStatus.setText("✅ Đã cập nhật tên thành công!");
        lblNameStatus.setStyle("-fx-text-fill: #10b981;");
    }

    @FXML
    public void handleChangePassword() {
        String username = App.getCurrentUser();
        if (username == null) {
            lblPasswordStatus.setText("Vui lòng đăng nhập để đổi mật khẩu.");
            lblPasswordStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (currentPassword == null || currentPassword.isBlank()) {
            lblPasswordStatus.setText("Vui lòng nhập mật khẩu hiện tại.");
            lblPasswordStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (!UserService.getInstance().authenticate(username, currentPassword)) {
            lblPasswordStatus.setText("Mật khẩu hiện tại không đúng.");
            lblPasswordStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (newPassword == null || newPassword.isBlank()) {
            lblPasswordStatus.setText("Vui lòng nhập mật khẩu mới.");
            lblPasswordStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (newPassword.length() < 6) {
            lblPasswordStatus.setText("Mật khẩu mới phải có ít nhất 6 ký tự.");
            lblPasswordStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            lblPasswordStatus.setText("Mật khẩu xác nhận không khớp.");
            lblPasswordStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (currentPassword.equals(newPassword)) {
            lblPasswordStatus.setText("Mật khẩu mới phải khác mật khẩu hiện tại.");
            lblPasswordStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        boolean changed = UserService.getInstance().changePassword(username, newPassword);
        if (!changed) {
            lblPasswordStatus.setText("Lỗi hệ thống: không thể cập nhật mật khẩu vào DB.");
            lblPasswordStatus.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
        lblPasswordStatus.setText("✅ Đổi mật khẩu thành công!");
        lblPasswordStatus.setStyle("-fx-text-fill: #10b981;");
    }

    @FXML
    public void handleBackToShop() {
        try {
            app.showShopScene();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
