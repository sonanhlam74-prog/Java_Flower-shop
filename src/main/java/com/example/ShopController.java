package com.example;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ShopController {
    @FXML
    private Label lblCartBadge;
    @FXML
    private Label lblInfo;
    @FXML
    private Label lblMemberBadge;
    @FXML
    private ImageView imgMemberBadge;
    @FXML
    private Label lblMemberProgress;
    @FXML
    private Label lblSmartSuggestions;
    @FXML
    private VBox viewMorePanel;
    @FXML
    private Label lblViewMoreTitle;
    @FXML
    private Label lblViewMoreContent;
    @FXML
    private TextField txtSearch;
    @FXML
    private FlowPane searchSuggestionPane;
    @FXML
    private ScrollPane shopScrollPane;
    @FXML
    private Slider sliderPrice;
    @FXML
    private Label lblPriceValue;
    @FXML
    private VBox priceFilterPanel;
    @FXML
    private Button btnPriceToggle;
    @FXML
    private Button btnCartIcon;
    @FXML
    private Button btnAvatar;
    @FXML
    private StackPane avatarWrap;
    @FXML
    private VBox avatarDropdown;
    @FXML
    private Label lblAvatarName;
    @FXML
    private Label lblAvatarTier;
    @FXML
    private Label lblAvatarSpent;
    @FXML
    private Button btnAvatarLogin;
    @FXML
    private Button btnAvatarLogout;
    @FXML
    private VBox sectionTet;
    @FXML
    private VBox sectionFuneral;
    @FXML
    private VBox sectionCelebrate;
    @FXML
    private VBox sectionBirthday;
    @FXML
    private VBox sectionWedding;
    @FXML
    private VBox cardMaiVang;
    @FXML
    private VBox cardDao;
    @FXML
    private VBox cardCucMamXoi;
    @FXML
    private VBox cardLanTet;
    @FXML
    private VBox cardCucTrang;
    @FXML
    private VBox cardHueTrang;
    @FXML
    private VBox cardLyTrang;
    @FXML
    private VBox cardHongTrang;
    @FXML
    private VBox cardHuongDuong;
    @FXML
    private VBox cardHongDo;
    @FXML
    private VBox cardTulip;
    @FXML
    private VBox cardCamChuong;
    @FXML
    private VBox cardHongPhan;
    @FXML
    private VBox cardDongTien;
    @FXML
    private VBox cardBabyTrang;
    @FXML
    private VBox cardLanMokara;
    @FXML
    private VBox cardHongPastel;
    @FXML
    private VBox cardCatTuong;
    @FXML
    private VBox cardMauDon;
    @FXML
    private VBox cardPhiYen;

    private App app;
    private final Map<String, VBox> flowerCards = new LinkedHashMap<>();
    private final Tooltip cartTooltip = new Tooltip();
    /** Lưu cache icon hạng để tránh tải lại mỗi lần làm mới */
    private final Map<MembershipStore.Tier, Image> tierIconCache = new java.util.EnumMap<>(MembershipStore.Tier.class);

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void initialize() {
        setupFlowerCards();
        loadFlowerImages();
        setupCartTooltip();
        showAllSections();
        viewMorePanel.setVisible(false);
        viewMorePanel.setManaged(false);
        renderSuggestionButtons("");
        setupPriceSlider();
        refreshStats();
        refreshAvatar();
        updateSmartInsights();
    }

    @FXML
    public void handleAddToCartBtn(ActionEvent event) {
        if (!(event.getSource() instanceof Button btn)) {
            return;
        }

        Object userData = btn.getUserData();
        if (!(userData instanceof String flowerName) || flowerName.isBlank()) {
            return;
        }

        if (!CartStore.addItem(flowerName)) {
            showOutOfStockAlert(flowerName);
            lblInfo.setText(flowerName + " đã hết hàng.");
            refreshStats();
            return;
        }

        lblInfo.setText("Đã thêm " + flowerName + " vào giỏ hàng.");
        refreshStats();
    }

    @FXML
    public void handleOpenAdminLogin() {
        try {
            if (App.isLoggedIn()) {
                app.showCrudScene(App.getCurrentUser());
            } else {
                app.showLoginScene();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            lblInfo.setText("Không thể mở trang quản trị.");
        }
    }

    @FXML
    public void handleOpenCheckout() {
        try {
            app.showCheckoutScene();
        } catch (Exception ex) {
            ex.printStackTrace();
            lblInfo.setText("Không thể mở trang thanh toán.");
        }
    }

    @FXML
    public void handleSearch() {
        applyCombinedFilter();
    }

    @FXML
    public void handleSearchTyping() {
        applyCombinedFilter();
    }

    @FXML
    public void handleClearSearch() {
        txtSearch.clear();
        sliderPrice.setValue(sliderPrice.getMax());
        priceFilterPanel.setVisible(false);
        priceFilterPanel.setManaged(false);
        applyCombinedFilter();
    }

    @FXML
    public void handleTogglePriceFilter() {
        boolean show = !priceFilterPanel.isVisible();
        priceFilterPanel.setVisible(show);
        priceFilterPanel.setManaged(show);
    }

    @FXML
    public void handleResetPrice() {
        sliderPrice.setValue(sliderPrice.getMax());
    }

    /* ===== Điều hướng cuộn danh mục ===== */

    private void scrollToSection(VBox section) {
        shopScrollPane.applyCss();
        shopScrollPane.layout();
        VBox content = (VBox) shopScrollPane.getContent();
        double contentHeight = content.getBoundsInLocal().getHeight();
        double viewportHeight = shopScrollPane.getViewportBounds().getHeight();
        double sectionY = section.getBoundsInParent().getMinY();
        double maxScroll = contentHeight - viewportHeight;
        if (maxScroll <= 0) return;
        shopScrollPane.setVvalue(Math.min(sectionY / maxScroll, 1.0));
    }

    @FXML
    public void handleScrollToTet() { scrollToSection(sectionTet); }
    @FXML
    public void handleScrollToFuneral() { scrollToSection(sectionFuneral); }
    @FXML
    public void handleScrollToCelebrate() { scrollToSection(sectionCelebrate); }
    @FXML
    public void handleScrollToBirthday() { scrollToSection(sectionBirthday); }
    @FXML
    public void handleScrollToWedding() { scrollToSection(sectionWedding); }

    @FXML
    public void handleViewMoreTet() {
        showCategoryDetails("Hoa tết");
    }

    @FXML
    public void handleViewMoreFuneral() {
        showCategoryDetails("Hoa Chia buồn");
    }

    @FXML
    public void handleViewMoreCelebrate() {
        showCategoryDetails("Hoa Chúc Mừng");
    }

    @FXML
    public void handleViewMoreBirthday() {
        showCategoryDetails("Hoa Sinh Nhật");
    }

    @FXML
    public void handleViewMoreWedding() {
        showCategoryDetails("Hoa Cưới");
    }

    @FXML
    public void handleOpenHistory() {
        avatarDropdown.setVisible(false);
        avatarDropdown.setManaged(false);
        try {
            app.showHistoryScene();
        } catch (Exception ex) {
            ex.printStackTrace();
            lblInfo.setText("Không thể mở lịch sử mua hàng.");
        }
    }

    @FXML
    public void handleCloseViewMore() {
        viewMorePanel.setVisible(false);
        viewMorePanel.setManaged(false);
    }

    private void setupCartTooltip() {
        cartTooltip.setShowDelay(Duration.millis(120));
        btnCartIcon.setTooltip(cartTooltip);
    }

    private void setupFlowerCards() {
        flowerCards.put("hoa mai vàng", cardMaiVang);
        flowerCards.put("hoa đào nhật tân", cardDao);
        flowerCards.put("hoa cúc mâm xôi", cardCucMamXoi);
        flowerCards.put("hoa lan hồ điệp tết", cardLanTet);
        flowerCards.put("hoa cúc trắng", cardCucTrang);
        flowerCards.put("hoa huệ trắng", cardHueTrang);
        flowerCards.put("hoa ly trắng", cardLyTrang);
        flowerCards.put("hoa hồng trắng", cardHongTrang);
        flowerCards.put("hoa hướng dương", cardHuongDuong);
        flowerCards.put("hoa hồng đỏ", cardHongDo);
        flowerCards.put("hoa tulip", cardTulip);
        flowerCards.put("hoa cẩm chướng", cardCamChuong);
        flowerCards.put("hoa hồng phấn", cardHongPhan);
        flowerCards.put("hoa đồng tiền", cardDongTien);
        flowerCards.put("hoa baby trắng", cardBabyTrang);
        flowerCards.put("hoa lan mokara", cardLanMokara);
        flowerCards.put("hoa hồng pastel", cardHongPastel);
        flowerCards.put("hoa cát tường", cardCatTuong);
        flowerCards.put("hoa mẫu đơn", cardMauDon);
        flowerCards.put("hoa phi yến", cardPhiYen);
        installFlowerTooltips();
    }

    /**
     * Thay thế emoji bằng hình ảnh thực từ URL trong CartStore.
     * Nếu URL trống → hiển thị placeholder text thay thế.
     */
    private void loadFlowerImages() {
        for (Map.Entry<String, VBox> entry : flowerCards.entrySet()) {
            String name = entry.getKey();
            VBox card = entry.getValue();
            CartStore.Product product = CartStore.getProduct(name);
            if (product == null || card.getChildren().isEmpty()) continue;
            if (!(card.getChildren().get(0) instanceof StackPane thumb)) continue;

            String imageUrl = resolveImageUrl(product.getImageUrl());
            if (imageUrl == null) continue; // giữ emoji khi chưa có ảnh

            // Xoá emoji label (giữ lại badge MỚI / HOT nếu có)
            thumb.getChildren().removeIf(n ->
                n instanceof Label lbl && lbl.getStyleClass().contains("flower-emoji"));

            try {
                Image image = new Image(imageUrl, 600, 400, true, true, true);
                ImageView iv = new ImageView(image);
                iv.fitWidthProperty().bind(thumb.widthProperty());
                iv.setFitHeight(200);
                iv.setPreserveRatio(false);
                iv.setSmooth(true);
                iv.getStyleClass().add("flower-image-view");

                // Clip bo góc trên để khớp card (border-radius 18)
                Rectangle clip = new Rectangle();
                clip.widthProperty().bind(thumb.widthProperty());
                clip.setHeight(200);
                clip.setArcWidth(36);
                clip.setArcHeight(36);
                iv.setClip(clip);

                thumb.getChildren().add(0, iv);
            } catch (Exception ex) {
                // URL không hợp lệ → giữ emoji
                Label fallback = new Label(product.getEmoji());
                fallback.getStyleClass().add("flower-emoji");
                thumb.getChildren().add(0, fallback);
            }
        }
    }

    /**
     * Chuyển đổi đường dẫn file hoặc URL thành dạng URL hợp lệ cho JavaFX Image.
     * Hỗ trợ: classpath resource (jar:...), URL web (https://...), file URL (file:///),
     * đường dẫn Windows (D:\...).
     * Trả về null nếu chuỗi trống.
     */
    private String resolveImageUrl(String raw) {
        if (raw == null || raw.isBlank()) return null;
        // Đã là URL hợp lệ (http, https, file, jar)
        if (raw.startsWith("http://") || raw.startsWith("https://")
                || raw.startsWith("file:/") || raw.startsWith("jar:")) {
            return raw;
        }
        // Đường dẫn file → chuyển thành file:// URL
        try {
            File file = new File(raw);
            if (file.exists()) {
                return file.toURI().toString();
            }
        } catch (Exception ignored) { }
        return raw; // trả về nguyên bản, để Image xử lý
    }

    private void installFlowerTooltips() {
        for (Map.Entry<String, VBox> entry : flowerCards.entrySet()) {
            String name = entry.getKey();
            VBox card = entry.getValue();
            Flower flower = InventoryStore.findFlowerByName(name);
            if (flower == null) continue;

            Tooltip tip = new Tooltip(
                flower.getName() + "\n"
                + "Giá: " + String.format("%,.0f", flower.getPrice()) + " VND\n"
                + "Tồn kho: " + flower.getStock() + "\n"
                + "Danh mục: " + flower.getCategory()
            );
            tip.setShowDelay(Duration.millis(200));
            tip.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
            Tooltip.install(card, tip);

            final String flowerName = flower.getName();
            card.setOnMouseClicked(e -> showFlowerDetail(flowerName));
            card.setStyle(card.getStyle() + "-fx-cursor: hand;");
        }
    }

    private void showAllSections() {
        setSectionVisible(true, true, true, true, true);
        for (VBox card : flowerCards.values()) {
            card.setVisible(true);
            card.setManaged(true);
        }
    }

    private void setupPriceSlider() {
        lblPriceValue.setText(formatVnd(sliderPrice.getValue()));
        sliderPrice.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblPriceValue.setText(formatVnd(newVal.doubleValue()));
            applyCombinedFilter();
        });
    }

    private static String formatVnd(double amount) {
        return String.format("%,.0f₫", amount);
    }

    private void applyCombinedFilter() {
        String raw = txtSearch.getText() == null ? "" : txtSearch.getText().trim();
        double maxPrice = sliderPrice.getValue();
        renderSuggestionButtons(raw.toLowerCase());

        boolean isDefaultState = raw.isBlank() && maxPrice >= sliderPrice.getMax();
        if (isDefaultState) {
            showAllSections();
            lblInfo.setText("Chào mừng bạn đến với cửa hàng hoa.");
            return;
        }

        java.util.List<String> keywords = new java.util.ArrayList<>();
        if (!raw.isBlank()) {
            for (String t : raw.toLowerCase().split(",")) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) keywords.add(trimmed);
            }
        }

        boolean found = false;
        for (Map.Entry<String, VBox> entry : flowerCards.entrySet()) {
            String name = entry.getKey();
            CartStore.Product product = CartStore.getProduct(name);
            double price = product != null ? product.getPrice() : 0;

            boolean matchesName = keywords.isEmpty();
            for (String kw : keywords) {
                if (name.contains(kw)) { matchesName = true; break; }
            }
            boolean matchesPrice = price <= maxPrice;
            boolean visible = matchesName && matchesPrice;

            entry.getValue().setVisible(visible);
            entry.getValue().setManaged(visible);
            found |= visible;
        }

        updateSectionVisibilityByCards();
        java.util.List<String> filters = new java.util.ArrayList<>();
        if (!raw.isBlank()) filters.add(raw);
        if (maxPrice < sliderPrice.getMax()) filters.add("≤ " + formatVnd(maxPrice));
        lblInfo.setText(found ? "Đang lọc: " + String.join(" + ", filters)
                              : "Không tìm thấy hoa phù hợp.");
    }

    private void renderSuggestionButtons(String query) {
        searchSuggestionPane.getChildren().clear();
        for (String flowerName : CartStore.getCatalog().keySet()) {
            String normalized = flowerName.toLowerCase();
            if (!query.isBlank() && !normalized.contains(query)) {
                continue;
            }

            Button suggestion = new Button(flowerName);
            suggestion.getStyleClass().addAll("action-btn", "ghost-btn", "shop-suggestion-btn");
            suggestion.setOnAction(event -> {
                txtSearch.setText(flowerName);
                applyCombinedFilter();
            });
            searchSuggestionPane.getChildren().add(suggestion);
        }

        boolean hasSuggestions = !searchSuggestionPane.getChildren().isEmpty();
        searchSuggestionPane.setVisible(hasSuggestions);
        searchSuggestionPane.setManaged(hasSuggestions);
    }

    private void updateSectionVisibilityByCards() {
        updateSectionVisibility(sectionTet);
        updateSectionVisibility(sectionFuneral);
        updateSectionVisibility(sectionCelebrate);
        updateSectionVisibility(sectionBirthday);
        updateSectionVisibility(sectionWedding);
    }

    private void updateSectionVisibility(VBox section) {
        boolean hasVisibleCard = false;
        for (Node child : section.getChildren()) {
            if (!(child instanceof HBox row)) {
                continue;
            }
            for (Node card : row.getChildren()) {
                if (card.isManaged()) {
                    hasVisibleCard = true;
                    break;
                }
            }
            if (hasVisibleCard) {
                break;
            }
        }

        section.setVisible(hasVisibleCard);
        section.setManaged(hasVisibleCard);
    }

    private void setSectionVisible(boolean showTet, boolean showFuneral, boolean showCelebrate, boolean showBirthday, boolean showWedding) {
        sectionTet.setVisible(showTet);
        sectionTet.setManaged(showTet);

        sectionFuneral.setVisible(showFuneral);
        sectionFuneral.setManaged(showFuneral);

        sectionCelebrate.setVisible(showCelebrate);
        sectionCelebrate.setManaged(showCelebrate);

        sectionBirthday.setVisible(showBirthday);
        sectionBirthday.setManaged(showBirthday);

        sectionWedding.setVisible(showWedding);
        sectionWedding.setManaged(showWedding);
    }

    private void refreshStats() {
        lblCartBadge.setText(String.valueOf(CartStore.getCartCount()));
        cartTooltip.setText(CartStore.buildCartPreview());
        refreshMembership();
        refreshSoldOut();
        updateSmartInsights();
    }

    private void updateSmartInsights() {
        List<String> bestSellers = getTopSellingNames(3);
        List<String> contextual = getContextualRecommendations(2);

        List<String> chunks = new ArrayList<>();
        if (!bestSellers.isEmpty()) {
            chunks.add("🔥 Bán chạy: " + String.join(", ", bestSellers));
        }
        if (!contextual.isEmpty()) {
            chunks.add("🎯 Hợp gu giỏ hàng: " + String.join(", ", contextual));
        }

        if (chunks.isEmpty()) {
            chunks.add("💡 Gợi ý hôm nay: Hoa Hồng Đỏ, Hoa Cát Tường, Hoa Lan Hồ Điệp Tết");
        }

        lblSmartSuggestions.setText(String.join("   •   ", chunks));
    }

    private List<String> getTopSellingNames(int limit) {
        List<String> result = new ArrayList<>();
        for (OrderHistoryStore.FlowerStat stat : OrderHistoryStore.getFlowerRanking()) {
            if (InventoryStore.getStock(stat.getName()) <= 0) {
                continue;
            }
            result.add(stat.getName());
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    private List<String> getContextualRecommendations(int limit) {
        Map<String, Integer> cart = CartStore.getCartSnapshot();
        Set<String> selectedCategories = new HashSet<>();
        Set<String> cartItems = new HashSet<>();

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            cartItems.add(normalizeKey(entry.getKey()));
            CartStore.Product p = CartStore.getProduct(entry.getKey());
            if (p != null) {
                selectedCategories.add(InventoryStore.normalizeCategory(p.getCategory()));
            }
        }

        if (selectedCategories.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<String> recommended = new ArrayList<>();
        for (CartStore.Product product : CartStore.getCatalog().values()) {
            String normalizedName = normalizeKey(product.getName());
            if (cartItems.contains(normalizedName)) {
                continue;
            }
            if (!selectedCategories.contains(InventoryStore.normalizeCategory(product.getCategory()))) {
                continue;
            }
            if (InventoryStore.getStock(product.getName()) <= 0) {
                continue;
            }
            recommended.add(product.getName());
            if (recommended.size() >= limit) {
                break;
            }
        }
        return recommended;
    }

    private String normalizeKey(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }

    private void refreshMembership() {
        MembershipStore.Tier tier = MembershipStore.getCurrentTier();
        lblMemberBadge.setText(tier.getDisplayName());
        lblMemberBadge.setStyle("-fx-border-color: " + tier.getColor() + "; -fx-text-fill: " + tier.getColor() + ";");

        // Tải icon hạng từ cache để tránh nhấp nháy
        try {
            Image cached = tierIconCache.computeIfAbsent(tier,
                    t -> new Image(t.getIconUrl(), 22, 22, true, true, true));
            if (imgMemberBadge.getImage() != cached) {
                imgMemberBadge.setImage(cached);
            }
        } catch (Exception ignored) { }

        double spent = MembershipStore.getTotalSpent();
        MembershipStore.Tier next = MembershipStore.getNextTier();
        if (next != null) {
            double remaining = MembershipStore.getAmountToNextTier();
            lblMemberProgress.setText(
                String.format("Tích lũy: %,.0f₫ — Còn %,.0f₫ để lên hạng %s", spent, remaining, next.getDisplayName())
            );
        } else {
            lblMemberProgress.setText(
                String.format("Tích lũy: %,.0f₫ — %s — Giảm %d%% cho mọi đơn hàng!",
                    spent, tier.getDisplayName(), (int)(tier.getDiscountRate() * 100))
            );
        }
    }

    private void showOutOfStockAlert(String flowerName) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText("Đã hết hoa");
        alert.setContentText("Sản phẩm " + flowerName + " đã hết hàng.");
        alert.showAndWait();
    }

    private void showCategoryDetails(String category) {
        List<Flower> flowers = InventoryStore.getAllFlowers();
        StringBuilder content = new StringBuilder();
        String normalizedSection = InventoryStore.normalizeCategory(category);

        for (Flower flower : flowers) {
            if (!normalizedSection.equals(InventoryStore.normalizeCategory(flower.getCategory()))) {
                continue;
            }

            if (content.length() > 0) {
                content.append("\n");
            }
            content.append("• ")
                    .append(flower.getName())
                    .append(" | Giá: ")
                    .append(String.format("%.0f", flower.getPrice()))
                    .append(" VND | Tồn: ")
                    .append(flower.getStock());
        }

        if (content.length() == 0) {
            content.append("Hiện chưa có hoa nào trong mục này.");
        }

        lblViewMoreTitle.setText("Danh sách " + normalizedSection + " (đồng bộ từ CRUD)");
        lblViewMoreContent.setText(content.toString());
        viewMorePanel.setVisible(true);
        viewMorePanel.setManaged(true);
    }

    /* ===== LỚP PHỦ HẾT HÀNG ===== */

    private void refreshSoldOut() {
        for (Map.Entry<String, VBox> entry : flowerCards.entrySet()) {
            String name = entry.getKey();
            VBox card = entry.getValue();
            int stock = InventoryStore.getStock(name);
            boolean soldOut = (stock <= 0);

            // Tìm StackPane (vùng ảnh) — con đầu tiên của card
            if (!card.getChildren().isEmpty() && card.getChildren().get(0) instanceof StackPane thumb) {
                thumb.getChildren().removeIf(n -> "sold-out-overlay".equals(n.getId()));

                if (soldOut) {
                    Label overlay = new Label("SOLD OUT");
                    overlay.setId("sold-out-overlay");
                    overlay.getStyleClass().add("sold-out-label");
                    overlay.setMouseTransparent(true);
                    StackPane.setAlignment(overlay, Pos.CENTER);
                    thumb.getChildren().add(overlay);
                }
            }

            // Vô hiệu/bật nút Thêm — bên trong HBox card-bottom-row
            for (Node child : card.lookupAll(".card-add-btn")) {
                if (child instanceof Button btn) {
                    btn.setDisable(soldOut);
                    if (soldOut) {
                        btn.setText("Hết hàng");
                    } else {
                        btn.setText("🛒 Add");
                    }
                }
            }

            card.setOpacity(soldOut ? 0.6 : 1.0);
        }
    }

    /* ===== MENU ẢNH ĐẠI DIỆN ===== */

    @FXML
    public void handleToggleAvatarMenu() {
        boolean show = !avatarDropdown.isVisible();
        avatarDropdown.setVisible(show);
        avatarDropdown.setManaged(show);
        if (show) {
            refreshAvatar();
            avatarDropdown.toFront();
        }
    }

    @FXML
    public void handleOpenProfile() {
        avatarDropdown.setVisible(false);
        avatarDropdown.setManaged(false);
        try {
            app.showProfileScene();
        } catch (Exception ex) {
            ex.printStackTrace();
            lblInfo.setText("Không thể mở trang hồ sơ.");
        }
    }

    @FXML
    public void handleAvatarLogin() {
        avatarDropdown.setVisible(false);
        avatarDropdown.setManaged(false);
        try {
            app.showLoginScene();
        } catch (Exception ex) {
            ex.printStackTrace();
            lblInfo.setText("Không thể mở trang đăng nhập.");
        }
    }

    @FXML
    public void handleAvatarLogout() {
        App.logout();
        avatarDropdown.setVisible(false);
        avatarDropdown.setManaged(false);
        refreshAvatar();
        refreshStats();
        lblInfo.setText("Đã đăng xuất. Bạn đang là khách.");
    }

    private void refreshAvatar() {
        boolean loggedIn = App.isLoggedIn();
        String name = App.getDisplayName();

        String initial = name.isEmpty() ? "G" : name.substring(0, 1).toUpperCase();
        btnAvatar.setText(initial);

        if (loggedIn) {
            btnAvatar.getStyleClass().removeAll("avatar-guest");
            if (!btnAvatar.getStyleClass().contains("avatar-logged-in")) {
                btnAvatar.getStyleClass().add("avatar-logged-in");
            }
        } else {
            btnAvatar.getStyleClass().removeAll("avatar-logged-in");
            if (!btnAvatar.getStyleClass().contains("avatar-guest")) {
                btnAvatar.getStyleClass().add("avatar-guest");
            }
        }

        lblAvatarName.setText(loggedIn ? name : "Khách (Guest)");
        MembershipStore.Tier tier = MembershipStore.getCurrentTier();
        lblAvatarTier.setText("Hạng: " + tier.getDisplayName());
        lblAvatarTier.setStyle("-fx-text-fill: " + tier.getColor() + ";");
        lblAvatarSpent.setText("Tích lũy: " + String.format("%,.0f", MembershipStore.getTotalSpent()) + "₫");

        btnAvatarLogin.setVisible(!loggedIn);
        btnAvatarLogin.setManaged(!loggedIn);
        btnAvatarLogout.setVisible(loggedIn);
        btnAvatarLogout.setManaged(loggedIn);
    }

    /* ===== POPUP CHI TIẾT HOA ===== */

    private void showFlowerDetail(String flowerName) {
        Flower flower = InventoryStore.findFlowerByName(flowerName);
        if (flower == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết hoa");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.getStylesheets().addAll(
            getClass().getResource("/com/example/common.css").toExternalForm(),
            getClass().getResource("/com/example/shop.css").toExternalForm()
        );
        pane.getStyleClass().add("flower-detail-dialog");
        pane.setPrefWidth(420);

        // Header: image hoặc emoji
        CartStore.Product product = CartStore.getProduct(flowerName);
        String imageUrl = (product != null) ? product.getImageUrl() : "";
        String resolvedPopupUrl = resolveImageUrl(imageUrl);
        Node headerNode;
        if (resolvedPopupUrl != null) {
            try {
                Image img = new Image(resolvedPopupUrl, 380, 220, true, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(380);
                iv.setFitHeight(220);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                StackPane imgWrap = new StackPane(iv);
                imgWrap.setStyle("-fx-background-radius: 12; -fx-padding: 4;");
                imgWrap.setMaxWidth(Double.MAX_VALUE);
                imgWrap.setAlignment(Pos.CENTER);
                headerNode = imgWrap;
            } catch (Exception ex) {
                Label emojiLabel = new Label(CartStore.getFlowerEmoji(flowerName));
                emojiLabel.setStyle("-fx-font-size: 72px;");
                emojiLabel.setMaxWidth(Double.MAX_VALUE);
                emojiLabel.setAlignment(Pos.CENTER);
                headerNode = emojiLabel;
            }
        } else {
            Label emojiLabel = new Label(CartStore.getFlowerEmoji(flowerName));
            emojiLabel.setStyle("-fx-font-size: 72px;");
            emojiLabel.setMaxWidth(Double.MAX_VALUE);
            emojiLabel.setAlignment(Pos.CENTER);
            headerNode = emojiLabel;
        }

        // Tên hoa
        Label nameLabel = new Label(flower.getName());
        nameLabel.getStyleClass().add("detail-flower-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setAlignment(Pos.CENTER);

        Separator sep1 = new Separator();

        // Các hàng thông tin
        Label categoryLabel = new Label("📁 Danh mục: " + flower.getCategory());
        categoryLabel.getStyleClass().add("detail-info");

        Label priceLabel = new Label("💰 Giá: " + String.format("%,.0f", flower.getPrice()) + "₫");
        priceLabel.getStyleClass().add("detail-info");

        Label stockLabel = new Label("📦 Tồn kho: " + flower.getStock() + " sản phẩm");
        stockLabel.getStyleClass().add("detail-info");

        boolean soldOut = flower.getStock() <= 0;
        Label statusLabel = new Label(soldOut ? "🔴 Hết hàng" : "🟢 Còn hàng");
        statusLabel.getStyleClass().add("detail-info");
        statusLabel.setStyle(soldOut
            ? "-fx-text-fill: #ef4444; -fx-font-weight: 800;"
            : "-fx-text-fill: #16a34a; -fx-font-weight: 800;");

        VBox infoBox = new VBox(6, categoryLabel, priceLabel, stockLabel, statusLabel);
        infoBox.setPadding(new Insets(0, 16, 0, 16));

        Separator sep2 = new Separator();

        // Nút thêm vào giỏ hàng
        Button addBtn = new Button("🛒 Thêm vào giỏ hàng");
        addBtn.getStyleClass().addAll("action-btn", "primary-btn");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setDisable(soldOut);
        addBtn.setOnAction(e -> {
            if (CartStore.addItem(flowerName)) {
                lblInfo.setText("Đã thêm " + flowerName + " vào giỏ hàng.");
                refreshStats();
                dialog.close();
            } else {
                showOutOfStockAlert(flowerName);
                refreshStats();
            }
        });

        HBox btnWrap = new HBox(addBtn);
        btnWrap.setPadding(new Insets(0, 16, 0, 16));
        addBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addBtn, javafx.scene.layout.Priority.ALWAYS);

        VBox content = new VBox(10, headerNode, nameLabel, sep1, infoBox, sep2, btnWrap);
        content.setPadding(new Insets(16));

        pane.setContent(content);
        dialog.showAndWait();
    }

}
