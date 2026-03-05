package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileController {
    @FXML private Label lblUsername;
    @FXML private Label lblEmail;
    @FXML private TextField txtFullName;
    @FXML private Label lblNameStatus;
    @FXML private Label lblTierIcon;
    @FXML private Label lblTierName;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotalSpent;
    @FXML private Label lblNextTier;
    @FXML private Label lblAmountToNext;

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
            txtFullName.setText("");
            txtFullName.setDisable(true);
        } else {
            lblUsername.setText(username);
            lblEmail.setText(UserStore.getEmail(username));
            txtFullName.setText(UserStore.getFullName(username));
            txtFullName.setDisable(false);
        }
        loadMembership();
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

        UserStore.updateFullName(username, newName);
        lblNameStatus.setText("✅ Đã cập nhật tên thành công!");
        lblNameStatus.setStyle("-fx-text-fill: #10b981;");
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
