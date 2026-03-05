package com.example;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CheckoutController {
    /* ---- Trường FXML: cột trái (thông tin người gửi) ---- */
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtAddress;
    @FXML private TextField txtNote;
    @FXML private ComboBox<String> cmbDeliveryZone;
    @FXML private ComboBox<String> cmbDeliverySpeed;
    @FXML private Label lblDeliveryEta;

    @FXML private Label errFullName;
    @FXML private Label errEmail;
    @FXML private Label errPhone;
    @FXML private Label errAddress;
    @FXML private Label errDeliveryZone;

    /* ---- Phương thức thanh toán ---- */
    @FXML private ToggleGroup paymentGroup;
    @FXML private RadioButton rbBank;
    @FXML private RadioButton rbVisa;
    @FXML private RadioButton rbEWallet;
    @FXML private Label errPayment;

    /* ---- Điều khoản ---- */
    @FXML private CheckBox cbTerms;
    @FXML private Label errTerms;

    /* ---- Cột phải (chi tiết đơn hàng) ---- */
    @FXML private VBox orderItemsBox;
    @FXML private TextField txtCoupon;
    @FXML private Label lblCouponStatus;

    @FXML private Label lblSubtotal;
    @FXML private Label lblTax;
    @FXML private Label lblDiscount;
    @FXML private Label lblDiscountLabel;
    @FXML private HBox discountRow;
    @FXML private Label lblCouponDiscount;
    @FXML private Label lblCouponDiscountLabel;
    @FXML private HBox couponDiscountRow;
    @FXML private Label lblShippingFee;
    @FXML private Label lblShippingLabel;
    @FXML private Label lblTotal;
    @FXML private Label lblMemberTier;
    @FXML private Label lblMemberInfo;

    @FXML private Button btnConfirm;
    @FXML private Label lblOrderStatus;

    /* ---- Hằng số ---- */
    private static final double TAX_RATE = 0.08;
    private static final NumberFormat VND_FORMAT = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    /* Mã giảm giá hợp lệ: mã → tỷ lệ giảm */
    private static final Map<String, Double> VALID_COUPONS = new LinkedHashMap<>();
    /* Giới hạn số lần sử dụng mã (chỉ áp dụng cho mã có giới hạn) */
    private static final Map<String, Integer> COUPON_MAX_USES = new LinkedHashMap<>();
    private static final Map<String, Integer> couponUsageCount = new LinkedHashMap<>();
    private static final Map<String, Integer> DELIVERY_ZONE_BASE_FEE = new LinkedHashMap<>();
    private static final Map<String, Double> DELIVERY_SPEED_MULTIPLIER = new LinkedHashMap<>();
    private static final Map<String, String> DELIVERY_SPEED_ETA = new LinkedHashMap<>();
    static {
        VALID_COUPONS.put("GIAM10", 0.10);
        VALID_COUPONS.put("GIAM20", 0.20);
        VALID_COUPONS.put("FLOWER50", 0.50);
        VALID_COUPONS.put("WELCOME", 0.15);
        VALID_COUPONS.put("DOMIXI", 0.36);

        /* Mã DOMIXI giới hạn 3 lần sử dụng */
        COUPON_MAX_USES.put("DOMIXI", 3);

        DELIVERY_ZONE_BASE_FEE.put("Nội thành", 20000);
        DELIVERY_ZONE_BASE_FEE.put("Cận thành", 35000);
        DELIVERY_ZONE_BASE_FEE.put("Liên tỉnh", 55000);

        DELIVERY_SPEED_MULTIPLIER.put("Tiêu chuẩn (24h)", 1.0);
        DELIVERY_SPEED_MULTIPLIER.put("Nhanh (4h)", 1.6);
        DELIVERY_SPEED_MULTIPLIER.put("Hỏa tốc (2h)", 2.2);

        DELIVERY_SPEED_ETA.put("Tiêu chuẩn (24h)", "Dự kiến giao trong 24 giờ");
        DELIVERY_SPEED_ETA.put("Nhanh (4h)", "Dự kiến giao trong 4 giờ");
        DELIVERY_SPEED_ETA.put("Hỏa tốc (2h)", "Dự kiến giao trong 2 giờ");
    }

    private App app;
    private double appliedCouponRate = 0;
    private String appliedCouponCode = null;

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void initialize() {
        setupDeliveryOptions();
        renderOrderItems();
        updateSummary();

        /* Tự động điền tên nếu đã đăng nhập */
        if (App.isLoggedIn()) {
            txtFullName.setText(App.getDisplayName());
        }
    }

    private void setupDeliveryOptions() {
        cmbDeliveryZone.setItems(FXCollections.observableArrayList(DELIVERY_ZONE_BASE_FEE.keySet()));
        cmbDeliverySpeed.setItems(FXCollections.observableArrayList(DELIVERY_SPEED_MULTIPLIER.keySet()));

        cmbDeliveryZone.getSelectionModel().select("Nội thành");
        cmbDeliverySpeed.getSelectionModel().select("Tiêu chuẩn (24h)");
        lblDeliveryEta.setText("📍 Dự kiến giao trong 24 giờ");

        cmbDeliveryZone.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                errDeliveryZone.setVisible(false);
                errDeliveryZone.setManaged(false);
            }
            updateSummary();
        });

        cmbDeliverySpeed.valueProperty().addListener((obs, oldVal, newVal) -> {
            refreshDeliveryEta();
            updateSummary();
        });
    }

    private void refreshDeliveryEta() {
        String speed = cmbDeliverySpeed.getValue();
        String eta = DELIVERY_SPEED_ETA.getOrDefault(speed, "Dự kiến giao trong ngày");
        lblDeliveryEta.setText("📍 " + eta);
    }

    /* ======================= ĐIỀU HƯỚNG ======================= */

    @FXML
    public void handleBackToShop() {
        try { app.showShopScene(); }
        catch (Exception ex) { showStatus("Không thể quay lại cửa hàng.", true); }
    }

    /* ======================= MÃ GIẢM GIÁ ======================= */

    @FXML
    public void handleApplyCoupon() {
        String code = txtCoupon.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            showCouponStatus("Vui lòng nhập mã giảm giá.", true);
            return;
        }

        Double rate = VALID_COUPONS.get(code);
        if (rate == null) {
            showCouponStatus("Mã giảm giá \"" + code + "\" không hợp lệ hoặc đã hết hạn.", true);
            appliedCouponRate = 0;
            appliedCouponCode = null;
        } else if (COUPON_MAX_USES.containsKey(code)) {
            int used = couponUsageCount.getOrDefault(code, 0);
            int max = COUPON_MAX_USES.get(code);
            if (used >= max) {
                showCouponStatus("Mã \"" + code + "\" đã hết lượt sử dụng (" + max + "/" + max + ").", true);
                appliedCouponRate = 0;
                appliedCouponCode = null;
            } else {
                appliedCouponRate = rate;
                appliedCouponCode = code;
                int remaining = max - used;
                showCouponStatus("Áp dụng mã \"" + code + "\" thành công! Giảm " + (int)(rate * 100) + "% (còn " + remaining + "/" + max + " lượt)", false);
            }
        } else {
            appliedCouponRate = rate;
            appliedCouponCode = code;
            showCouponStatus("Áp dụng mã \"" + code + "\" thành công! Giảm " + (int)(rate * 100) + "%", false);
        }
        updateSummary();
    }

    /* ======================= XÁC NHẬN ĐƠN HÀNG ======================= */

    @FXML
    public void handleConfirmOrder() {
        boolean valid = validateAll();
        if (!valid) {
            showStatus("Vui lòng kiểm tra lại thông tin bên trên.", true);
            return;
        }

        if (CartStore.getCartCount() == 0) {
            showStatus("Giỏ hàng đang trống, không thể đặt hàng.", true);
            return;
        }

        double subtotal = CartStore.getSubtotal();
        double tax = subtotal * TAX_RATE;
        double memberDiscount = (subtotal + tax) * MembershipStore.getDiscountRate();
        double afterMember = subtotal + tax - memberDiscount;
        double couponDiscount = afterMember * appliedCouponRate;
        double shippingFee = calculateShippingFee(subtotal);
        double total = afterMember - couponDiscount + shippingFee;

        /* Tăng số lần sử dụng mã giảm giá nếu có giới hạn */
        if (appliedCouponCode != null && COUPON_MAX_USES.containsKey(appliedCouponCode)) {
            couponUsageCount.merge(appliedCouponCode, 1, Integer::sum);
        }

        MembershipStore.recordPurchase(total);
        OrderHistoryStore.recordOrder(CartStore.getCartSnapshot(), subtotal, tax, memberDiscount + couponDiscount, total,
                txtFullName.getText().trim());
        CartStore.clearCartAfterPurchase();

        MembershipStore.Tier tier = MembershipStore.getCurrentTier();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đặt hàng thành công");
        alert.setHeaderText("🎉 Đơn hàng đã được xác nhận!");
        String paymentMethod = rbBank != null && rbBank.isSelected() ? "Chuyển khoản ngân hàng"
                             : rbVisa != null && rbVisa.isSelected() ? "Visa / Mastercard"
                             : "Ví điện tử";
        String zone = cmbDeliveryZone.getValue();
        String speed = cmbDeliverySpeed.getValue();
        String eta = DELIVERY_SPEED_ETA.getOrDefault(speed, "Trong ngày");
        alert.setContentText(String.format(
            "Người nhận: %s\nĐịa chỉ: %s\nKhu vực: %s\nTốc độ: %s (%s)\nThanh toán: %s\nPhí giao: %s\nTổng: %s\nHạng thành viên: %s\nTích lũy: %,.0f VND",
            txtFullName.getText().trim(), txtAddress.getText().trim(),
            zone, speed, eta, paymentMethod, formatVnd(shippingFee), formatVnd(total),
            tier.getDisplayName(), MembershipStore.getTotalSpent()
        ));
        alert.showAndWait();

        try { app.showShopScene(); }
        catch (Exception ex) { }
    }

    /* ======================= KIỂM TRA ======================= */

    private boolean validateAll() {
        boolean ok = true;
        ok &= validateRequired(txtFullName, errFullName, "Vui lòng nhập họ tên");
        ok &= validateEmail(txtEmail, errEmail);
        ok &= validatePhone(txtPhone, errPhone);
        ok &= validateRequired(txtAddress, errAddress, "Vui lòng nhập địa chỉ giao hàng");
        ok &= validateDelivery();
        ok &= validatePayment();
        ok &= validateTerms();
        return ok;
    }

    private boolean validateDelivery() {
        String zone = cmbDeliveryZone.getValue();
        if (zone == null || zone.isBlank()) {
            errDeliveryZone.setText("Vui lòng chọn khu vực giao hàng");
            errDeliveryZone.setVisible(true);
            errDeliveryZone.setManaged(true);
            return false;
        }

        errDeliveryZone.setVisible(false);
        errDeliveryZone.setManaged(false);
        return true;
    }

    private boolean validateRequired(TextField field, Label err, String msg) {
        if (field.getText().trim().isEmpty()) {
            showFieldError(field, err, msg);
            return false;
        }
        clearFieldError(field, err);
        return true;
    }

    private boolean validateEmail(TextField field, Label err) {
        String val = field.getText().trim();
        if (val.isEmpty()) {
            showFieldError(field, err, "Vui lòng nhập email");
            return false;
        }
        if (!val.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showFieldError(field, err, "Email không đúng định dạng (vd: abc@mail.com)");
            return false;
        }
        clearFieldError(field, err);
        return true;
    }

    private boolean validatePhone(TextField field, Label err) {
        String val = field.getText().trim();
        if (val.isEmpty()) {
            showFieldError(field, err, "Vui lòng nhập số điện thoại");
            return false;
        }
        if (!val.matches("^0\\d{9,10}$")) {
            showFieldError(field, err, "Số điện thoại không hợp lệ (10-11 số, bắt đầu bằng 0)");
            return false;
        }
        clearFieldError(field, err);
        return true;
    }

    private boolean validatePayment() {
        if (paymentGroup.getSelectedToggle() == null) {
            errPayment.setText("Vui lòng chọn phương thức thanh toán");
            errPayment.setVisible(true);
            errPayment.setManaged(true);
            return false;
        }
        errPayment.setVisible(false);
        errPayment.setManaged(false);
        return true;
    }

    private boolean validateTerms() {
        if (!cbTerms.isSelected()) {
            errTerms.setText("Bạn cần đồng ý với Điều khoản & Điều kiện");
            errTerms.setVisible(true);
            errTerms.setManaged(true);
            return false;
        }
        errTerms.setVisible(false);
        errTerms.setManaged(false);
        return true;
    }

    private void showFieldError(TextField field, Label err, String msg) {
        err.setText(msg);
        err.setVisible(true);
        err.setManaged(true);
        if (!field.getStyleClass().contains("checkout-input-error")) {
            field.getStyleClass().add("checkout-input-error");
        }
    }

    private void clearFieldError(TextField field, Label err) {
        err.setText("");
        err.setVisible(false);
        err.setManaged(false);
        field.getStyleClass().remove("checkout-input-error");
    }

    /* ======================= HIỂN THỊ MỤC ĐƠN HÀNG ======================= */

    private void renderOrderItems() {
        orderItemsBox.getChildren().clear();
        Map<String, Integer> snapshot = CartStore.getCartSnapshot();
        boolean hasItem = false;

        for (Map.Entry<String, Integer> entry : snapshot.entrySet()) {
            if (entry.getValue() <= 0) continue;
            hasItem = true;

            String productName = entry.getKey();
            int qty = entry.getValue();
            CartStore.Product product = CartStore.getProduct(productName);

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("checkout-item-row");

            Label emoji = new Label(product.getEmoji());
            emoji.getStyleClass().add("checkout-item-emoji");

            VBox info = new VBox(2);
            Label name = new Label(product.getName());
            name.getStyleClass().add("checkout-item-name");
            Label detail = new Label("SL: " + qty + "  ×  " + formatVnd(product.getPrice()));
            detail.getStyleClass().add("checkout-item-detail");
            info.getChildren().addAll(name, detail);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label lineTotal = new Label(formatVnd(product.getPrice() * qty));
            lineTotal.getStyleClass().add("checkout-item-total");

            Button btnDelete = new Button("✕");
            btnDelete.getStyleClass().add("checkout-item-delete");
            btnDelete.setOnAction(e -> {
                CartStore.removeAllOfItem(productName);
                renderOrderItems();
                updateSummary();
            });

            row.getChildren().addAll(emoji, info, lineTotal, btnDelete);
            orderItemsBox.getChildren().add(row);
        }

        if (!hasItem) {
            Label empty = new Label("Giỏ hàng trống — hãy thêm hoa trước khi thanh toán 🌸");
            empty.getStyleClass().add("checkout-empty-text");
            empty.setWrapText(true);
            orderItemsBox.getChildren().add(empty);
        }
    }

    /* ======================= TỔNG KẼT ======================= */

    private void updateSummary() {
        double subtotal = CartStore.getSubtotal();
        double tax = subtotal * TAX_RATE;
        double memberRate = MembershipStore.getDiscountRate();
        double memberDiscount = (subtotal + tax) * memberRate;
        double afterMember = subtotal + tax - memberDiscount;
        double couponDiscount = afterMember * appliedCouponRate;
        double shippingFee = calculateShippingFee(subtotal);
        double total = afterMember - couponDiscount + shippingFee;

        lblSubtotal.setText(formatVnd(subtotal));
        lblTax.setText(formatVnd(tax));

        if (memberRate > 0) {
            discountRow.setVisible(true);
            discountRow.setManaged(true);
            lblDiscountLabel.setText("Giảm giá thành viên (" + (int)(memberRate * 100) + "%)");
            lblDiscount.setText("-" + formatVnd(memberDiscount));
        } else {
            discountRow.setVisible(false);
            discountRow.setManaged(false);
        }

        if (appliedCouponRate > 0) {
            couponDiscountRow.setVisible(true);
            couponDiscountRow.setManaged(true);
            lblCouponDiscountLabel.setText("Mã " + appliedCouponCode + " (" + (int)(appliedCouponRate * 100) + "%)");
            lblCouponDiscount.setText("-" + formatVnd(couponDiscount));
        } else {
            couponDiscountRow.setVisible(false);
            couponDiscountRow.setManaged(false);
        }

        lblShippingLabel.setText(buildShippingLabel(subtotal));
        lblShippingFee.setText(formatVnd(shippingFee));

        lblTotal.setText(formatVnd(total));

        MembershipStore.Tier tier = MembershipStore.getCurrentTier();
        lblMemberTier.setText(tier.getDisplayName());
        lblMemberTier.setStyle("-fx-text-fill: " + tier.getColor() + "; -fx-border-color: " + tier.getColor() + ";");
        lblMemberInfo.setText(MembershipStore.getSummary());

        refreshDeliveryEta();
    }

    private String buildShippingLabel(double subtotal) {
        String speed = cmbDeliverySpeed.getValue();
        if (speed == null || speed.isBlank()) {
            speed = "Tiêu chuẩn (24h)";
        }
        if (subtotal >= 800000 && "Tiêu chuẩn (24h)".equals(speed)) {
            return "Phí giao hàng (Miễn phí đơn từ 800.000 VND)";
        }
        return "Phí giao hàng";
    }

    private double calculateShippingFee(double subtotal) {
        String zone = cmbDeliveryZone.getValue();
        String speed = cmbDeliverySpeed.getValue();

        int baseFee = DELIVERY_ZONE_BASE_FEE.getOrDefault(zone, 20000);
        double speedMultiplier = DELIVERY_SPEED_MULTIPLIER.getOrDefault(speed, 1.0);

        if (subtotal >= 800000 && "Tiêu chuẩn (24h)".equals(speed)) {
            return 0;
        }

        double shippingFee = baseFee * speedMultiplier;
        if ("Hỏa tốc (2h)".equals(speed) && subtotal < 300000) {
            shippingFee += 15000;
        }

        return Math.round(shippingFee / 1000.0) * 1000;
    }

    /* ======================= HÀM HỖ TRỢ ======================= */

    private void showCouponStatus(String msg, boolean isError) {
        lblCouponStatus.setText(msg);
        lblCouponStatus.setVisible(true);
        lblCouponStatus.setManaged(true);
        lblCouponStatus.getStyleClass().removeAll("checkout-coupon-ok", "checkout-coupon-fail");
        lblCouponStatus.getStyleClass().add(isError ? "checkout-coupon-fail" : "checkout-coupon-ok");
    }

    private void showStatus(String msg, boolean isError) {
        lblOrderStatus.setText(msg);
        lblOrderStatus.getStyleClass().removeAll("checkout-status-ok", "checkout-status-fail");
        lblOrderStatus.getStyleClass().add(isError ? "checkout-status-fail" : "checkout-status-ok");
    }

    private String formatVnd(double amount) {
        return VND_FORMAT.format(amount) + " VND";
    }
}