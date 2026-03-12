package com.example;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class CrudController {
    private static final String ALERT_TITLE = "Thông báo hệ thống";
    private static final NumberFormat VND = NumberFormat.getInstance(new Locale("vi", "VN"));

    /* ====== Nút thanh bên ====== */
    @FXML private Button btnMenuDashboard;
    @FXML private Button btnMenuProducts;
    @FXML private Button btnMenuStats;
    @FXML private Button btnMenuOrders;

    /* ====== Bảng điều khiển ====== */
    @FXML private VBox panelDashboard;
    @FXML private VBox panelProducts;
    @FXML private ScrollPane panelStats;
    @FXML private VBox panelOrders;

    /* ====== Bảng tổng quan ====== */
    @FXML private Label statTotal;
    @FXML private Label statInStock;
    @FXML private Label statLowStock;
    @FXML private Label statOrderCount;
    @FXML private Label statRevenue;
    @FXML private Label statCustomers;
    @FXML private Label lblProfileName;

    @FXML private TableView<OrderHistoryStore.Order> recentOrdersTable;
    @FXML private TableColumn<OrderHistoryStore.Order, Integer> colRecentId;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colRecentDate;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colRecentCustomer;
    @FXML private TableColumn<OrderHistoryStore.Order, Integer> colRecentItems;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colRecentTotal;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colRecentTier;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colRecentStatus;

    /* ====== Bảng sản phẩm ====== */
    @FXML private TableView<Flower> flowerTable;
    @FXML private TableColumn<Flower, Integer> colId;
    @FXML private TableColumn<Flower, String> colName;
    @FXML private TableColumn<Flower, String> colCategory;
    @FXML private TableColumn<Flower, Double> colPrice;
    @FXML private TableColumn<Flower, Integer> colStock;
    @FXML private TableColumn<Flower, String> colStatus;
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtStock;

    /* ====== Bảng thống kê ====== */
    @FXML private Label sFlowersSold;
    @FXML private Label sRevenue;
    @FXML private Label sPurchaseRate;

    // Xếp hạng hoa
    @FXML private TableView<OrderHistoryStore.FlowerStat> flowerRankTable;
    @FXML private TableColumn<OrderHistoryStore.FlowerStat, Integer> colFrRank;
    @FXML private TableColumn<OrderHistoryStore.FlowerStat, String> colFrEmoji;
    @FXML private TableColumn<OrderHistoryStore.FlowerStat, String> colFrName;
    @FXML private TableColumn<OrderHistoryStore.FlowerStat, Integer> colFrSold;
    @FXML private TableColumn<OrderHistoryStore.FlowerStat, String> colFrRevenue;

    // Xếp hạng khách hàng
    @FXML private TableView<OrderHistoryStore.CustomerStat> customerRankTable;
    @FXML private TableColumn<OrderHistoryStore.CustomerStat, Integer> colCrRank;
    @FXML private TableColumn<OrderHistoryStore.CustomerStat, String> colCrBadge;
    @FXML private TableColumn<OrderHistoryStore.CustomerStat, String> colCrName;
    @FXML private TableColumn<OrderHistoryStore.CustomerStat, Integer> colCrOrders;
    @FXML private TableColumn<OrderHistoryStore.CustomerStat, Integer> colCrItems;
    @FXML private TableColumn<OrderHistoryStore.CustomerStat, String> colCrSpent;

    // Doanh thu hàng ngày
    @FXML private AreaChart<String, Number> revenueAreaChart;
    @FXML private TableView<OrderHistoryStore.DailyRevenue> dailyRevenueTable;
    @FXML private TableColumn<OrderHistoryStore.DailyRevenue, String> colDrDate;
    @FXML private TableColumn<OrderHistoryStore.DailyRevenue, Integer> colDrOrders;
    @FXML private TableColumn<OrderHistoryStore.DailyRevenue, String> colDrRevenue;

    // Chi tiết khách hàng
    @FXML private ComboBox<String> cbCustomerFilter;
    @FXML private TableView<OrderHistoryStore.FlowerStat> customerDetailTable;
    @FXML private TableColumn<OrderHistoryStore.FlowerStat, String> colCdEmoji;
    @FXML private TableColumn<OrderHistoryStore.FlowerStat, String> colCdName;
    @FXML private TableColumn<OrderHistoryStore.FlowerStat, Integer> colCdQty;
    @FXML private TableColumn<OrderHistoryStore.FlowerStat, String> colCdTotal;

    /* ====== Bảng đơn hàng ====== */
    @FXML private ComboBox<String> cbOrderStatus;
    @FXML private TableView<OrderHistoryStore.Order> allOrdersTable;
    @FXML private TableColumn<OrderHistoryStore.Order, Integer> colOrdId;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colOrdDate;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colOrdCustomer;
    @FXML private TableColumn<OrderHistoryStore.Order, Integer> colOrdItems;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colOrdTotal;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colOrdTier;
    @FXML private TableColumn<OrderHistoryStore.Order, String> colOrdStatus;

    private final ObservableList<Flower> flowers = FXCollections.observableArrayList();
    private App app;

    public void setApp(App app) {
        this.app = app;
    }

    /* ================================================================
       KHỞI TẠO
       ================================================================ */
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStatus.setCellValueFactory(cd -> {
            int stock = cd.getValue().getStock();
            return new SimpleStringProperty(stock > 0 ? "Còn hàng" : "Hết hàng");
        });

        cbCategory.setItems(FXCollections.observableArrayList(
            "Hoa tết", "Hoa Chia buồn", "Hoa Chúc Mừng", "Hoa Sinh Nhật", "Hoa Cưới"
        ));
        cbCategory.getSelectionModel().selectFirst();

        flowerTable.setItems(flowers);
        reloadTableData();
        flowerTable.getSelectionModel().selectedItemProperty().addListener((o, ov, sel) -> fillForm(sel));

        /* Bảng đơn hàng gần đây (Tổng quan) */
        initOrderTable(colRecentId, colRecentDate, colRecentCustomer, colRecentItems, colRecentTotal, colRecentTier, colRecentStatus);

        /* Bảng tất cả đơn hàng */
        initOrderTable(colOrdId, colOrdDate, colOrdCustomer, colOrdItems, colOrdTotal, colOrdTier, colOrdStatus);

        /* Bảng xếp hạng hoa */
        colFrRank.setCellValueFactory(cd -> {
            int idx = flowerRankTable.getItems().indexOf(cd.getValue()) + 1;
            return new SimpleIntegerProperty(idx).asObject();
        });
        colFrEmoji.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEmoji()));
        colFrName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        colFrSold.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getTotalSold()).asObject());
        colFrRevenue.setCellValueFactory(cd -> new SimpleStringProperty(fmtVnd(cd.getValue().getTotalRevenue())));

        /* Bảng xếp hạng khách hàng */
        colCrRank.setCellValueFactory(cd -> {
            int idx = customerRankTable.getItems().indexOf(cd.getValue()) + 1;
            return new SimpleIntegerProperty(idx).asObject();
        });
        colCrBadge.setCellValueFactory(cd -> {
            int idx = customerRankTable.getItems().indexOf(cd.getValue()) + 1;
            String badge = idx <= 3 ? "🥇🥈🥉".substring((idx - 1) * 2, idx * 2) : (idx <= 10 ? "⭐" : "");
            return new SimpleStringProperty(badge);
        });
        colCrName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        colCrOrders.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getOrderCount()).asObject());
        colCrItems.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getTotalItems()).asObject());
        colCrSpent.setCellValueFactory(cd -> new SimpleStringProperty(fmtVnd(cd.getValue().getTotalSpent())));

        /* Bảng doanh thu hàng ngày */
        colDrDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate()));
        colDrOrders.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getOrderCount()).asObject());
        colDrRevenue.setCellValueFactory(cd -> new SimpleStringProperty(fmtVnd(cd.getValue().getRevenue())));

        /* Bảng chi tiết khách hàng */
        colCdEmoji.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEmoji()));
        colCdName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        colCdQty.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getTotalSold()).asObject());
        colCdTotal.setCellValueFactory(cd -> new SimpleStringProperty(fmtVnd(cd.getValue().getTotalRevenue())));

        // Định dạng các hàng xếp hạng
        customerRankTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(OrderHistoryStore.CustomerStat item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("top-1", "top-2", "top-3", "top-10");
                if (empty || item == null) return;
                int idx = getIndex() + 1;
                if (idx == 1) getStyleClass().add("top-1");
                else if (idx == 2) getStyleClass().add("top-2");
                else if (idx == 3) getStyleClass().add("top-3");
                else if (idx <= 10) getStyleClass().add("top-10");
            }
        });

        // Định dạng hàng đơn theo trạng thái
        javafx.util.Callback<TableView<OrderHistoryStore.Order>, TableRow<OrderHistoryStore.Order>> orderRowFactory = tv2 -> new TableRow<>() {
            @Override
            protected void updateItem(OrderHistoryStore.Order item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("order-delivering", "order-completed", "order-cancelled");
                if (empty || item == null) return;
                switch (item.getStatus()) {
                    case OrderHistoryStore.STATUS_DELIVERING -> getStyleClass().add("order-delivering");
                    case OrderHistoryStore.STATUS_COMPLETED  -> getStyleClass().add("order-completed");
                    case OrderHistoryStore.STATUS_CANCELLED  -> getStyleClass().add("order-cancelled");
                }
            }
        };
        recentOrdersTable.setRowFactory(orderRowFactory);
        allOrdersTable.setRowFactory(orderRowFactory);

        refreshDashboard();
        showPanel("dashboard");
    }

    private void initOrderTable(
            TableColumn<OrderHistoryStore.Order, Integer> cId,
            TableColumn<OrderHistoryStore.Order, String> cDate,
            TableColumn<OrderHistoryStore.Order, String> cCust,
            TableColumn<OrderHistoryStore.Order, Integer> cItems,
            TableColumn<OrderHistoryStore.Order, String> cTotal,
            TableColumn<OrderHistoryStore.Order, String> cTier,
            TableColumn<OrderHistoryStore.Order, String> cStatus) {
        cId.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getOrderId()).asObject());
        cDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDateTime()));
        cCust.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCustomerName()));
        cItems.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getItemCount()).asObject());
        cTotal.setCellValueFactory(cd -> new SimpleStringProperty(fmtVnd(cd.getValue().getTotal())));
        cTier.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTierAtPurchase()));
        cStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus()));
        cStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll("status-pill-delivering", "status-pill-completed", "status-pill-cancelled");
                if (empty || status == null || status.isBlank()) {
                    setText(null);
                    return;
                }

                setText(status);
                if (OrderHistoryStore.STATUS_DELIVERING.equals(status)) {
                    getStyleClass().add("status-pill-delivering");
                } else if (OrderHistoryStore.STATUS_COMPLETED.equals(status)) {
                    getStyleClass().add("status-pill-completed");
                } else if (OrderHistoryStore.STATUS_CANCELLED.equals(status)) {
                    getStyleClass().add("status-pill-cancelled");
                }
            }
        });
    }

    /* ================================================================
       ĐIỀU HƯỚNG THANH BÊN
       ================================================================ */
    @FXML public void handleMenuDashboard() { showPanel("dashboard"); refreshDashboard(); }
    @FXML public void handleMenuProducts()  { showPanel("products"); }
    @FXML public void handleMenuStats()     { showPanel("stats"); refreshStatistics(); }
    @FXML public void handleMenuOrders()    { showPanel("orders"); refreshOrders(); }

    private void showPanel(String panel) {
        panelDashboard.setVisible("dashboard".equals(panel));
        panelDashboard.setManaged("dashboard".equals(panel));
        panelProducts.setVisible("products".equals(panel));
        panelProducts.setManaged("products".equals(panel));
        panelStats.setVisible("stats".equals(panel));
        panelStats.setManaged("stats".equals(panel));
        panelOrders.setVisible("orders".equals(panel));
        panelOrders.setManaged("orders".equals(panel));

        for (Button btn : new Button[]{btnMenuDashboard, btnMenuProducts, btnMenuStats, btnMenuOrders}) {
            btn.getStyleClass().remove("menu-item-active");
        }
        switch (panel) {
            case "dashboard" -> btnMenuDashboard.getStyleClass().add("menu-item-active");
            case "products"  -> btnMenuProducts.getStyleClass().add("menu-item-active");
            case "stats"     -> btnMenuStats.getStyleClass().add("menu-item-active");
            case "orders"    -> btnMenuOrders.getStyleClass().add("menu-item-active");
        }
    }

    /* ================================================================
       TỔNG QUAN
       ================================================================ */
    private void refreshDashboard() {
        int total = flowers.size();
        int inStock = flowers.stream().mapToInt(Flower::getStock).sum();
        long lowStock = flowers.stream().filter(f -> f.getStock() > 0 && f.getStock() <= 15).count();

        statTotal.setText(String.valueOf(total));
        statInStock.setText(String.valueOf(inStock));
        statLowStock.setText(String.valueOf(lowStock));
        statOrderCount.setText(String.valueOf(OrderHistoryStore.getOrderCount()));
        statRevenue.setText(fmtVnd(OrderHistoryStore.getTotalRevenue()));
        statCustomers.setText(String.valueOf(OrderHistoryStore.getUniqueCustomerCount()));

        // Đơn hàng gần đây (tối đa 10)
        List<OrderHistoryStore.Order> all = OrderHistoryStore.getAllOrders();
        List<OrderHistoryStore.Order> recent = all.subList(0, Math.min(10, all.size()));
        recentOrdersTable.setItems(FXCollections.observableArrayList(recent));
    }

    /* ================================================================
       THỐNG KÊ
       ================================================================ */
    @FXML
    public void handleRefreshStats() {
        refreshStatistics();
    }

    private void refreshStatistics() {
        sFlowersSold.setText(String.valueOf(OrderHistoryStore.getTotalFlowersSold()));
        sRevenue.setText(fmtVnd(OrderHistoryStore.getTotalRevenue()));
        double rate = OrderHistoryStore.getPurchaseRate();
        sPurchaseRate.setText(String.format("%.1f đơn/KH", rate));

        flowerRankTable.setItems(FXCollections.observableArrayList(OrderHistoryStore.getFlowerRanking()));

        customerRankTable.setItems(FXCollections.observableArrayList(OrderHistoryStore.getCustomerRanking()));

        dailyRevenueTable.setItems(FXCollections.observableArrayList(OrderHistoryStore.getDailyRevenue()));

        /* Biểu đồ miền doanh thu */
        populateRevenueChart();

        List<OrderHistoryStore.CustomerStat> customers = OrderHistoryStore.getCustomerRanking();
        ObservableList<String> names = FXCollections.observableArrayList();
        for (OrderHistoryStore.CustomerStat cs : customers) names.add(cs.getName());
        cbCustomerFilter.setItems(names);
        if (!names.isEmpty()) cbCustomerFilter.getSelectionModel().selectFirst();

        customerDetailTable.getItems().clear();
    }

    @FXML
    public void handleViewCustomerDetail() {
        String selected = cbCustomerFilter.getValue();
        if (selected == null || selected.isBlank()) {
            showAlert("Chưa chọn", "Vui lòng chọn khách hàng để xem chi tiết.");
            return;
        }
        List<OrderHistoryStore.FlowerStat> details = OrderHistoryStore.getFlowersByCustomer(selected);
        customerDetailTable.setItems(FXCollections.observableArrayList(details));
    }

    /* ======================= BIỂU ĐỒ DOANH THU ======================= */

    private void populateRevenueChart() {
        revenueAreaChart.getData().clear();
        List<OrderHistoryStore.DailyRevenue> dailyData = OrderHistoryStore.getDailyRevenue();

        // Đảo ngược để ngày cũ ở bên trái, ngày mới ở bên phải
        List<OrderHistoryStore.DailyRevenue> reversed = new java.util.ArrayList<>(dailyData);
        java.util.Collections.reverse(reversed);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        for (OrderHistoryStore.DailyRevenue dr : reversed) {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(dr.getDate(), dr.getRevenue());
            series.getData().add(dataPoint);
        }

        revenueAreaChart.getData().add(series);

        // Thêm tooltip khi trỏ chuột vào từng điểm dữ liệu
        for (int i = 0; i < series.getData().size(); i++) {
            XYChart.Data<String, Number> dp = series.getData().get(i);
            OrderHistoryStore.DailyRevenue dr = reversed.get(i);
            javafx.scene.Node node = dp.getNode();
            if (node != null) {
                String tooltipText = String.format("Ng\u00e0y: %s\nDoanh thu: %s\nS\u1ed1 \u0111\u01a1n: %d",
                        dr.getDate(), fmtVnd(dr.getRevenue()), dr.getOrderCount());
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
                Tooltip.install(node, tooltip);
                node.setStyle("-fx-cursor: hand;");

                // Hiệu ứng phóng to khi hover
                node.setOnMouseEntered(e -> node.setScaleX(1.5));
                node.setOnMouseEntered(e -> { node.setScaleX(1.5); node.setScaleY(1.5); });
                node.setOnMouseExited(e -> { node.setScaleX(1.0); node.setScaleY(1.0); });
            }
        }
    }

    /* ================================================================
       ĐƠN HÀNG
       ================================================================ */
    private void refreshOrders() {
        allOrdersTable.setItems(FXCollections.observableArrayList(OrderHistoryStore.getAllOrders()));
        if (cbOrderStatus.getItems().isEmpty()) {
            cbOrderStatus.setItems(FXCollections.observableArrayList(
                OrderHistoryStore.STATUS_DELIVERING,
                OrderHistoryStore.STATUS_COMPLETED,
                OrderHistoryStore.STATUS_CANCELLED
            ));
            cbOrderStatus.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void handleUpdateOrderStatus() {
        OrderHistoryStore.Order selected = allOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn đơn hàng", "Vui lòng chọn một đơn hàng trong bảng để cập nhật trạng thái.");
            return;
        }
        String newStatus = cbOrderStatus.getValue();
        if (newStatus == null || newStatus.isBlank()) {
            showAlert("Chưa chọn trạng thái", "Vui lòng chọn trạng thái mới.");
            return;
        }
        selected.setStatus(newStatus);
        allOrdersTable.refresh();
        recentOrdersTable.refresh();
        refreshDashboard();
        refreshStatistics();

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Thành công");
        info.setHeaderText("Cập nhật trạng thái");
        info.setContentText("Đơn hàng #" + selected.getOrderId() + " → " + newStatus);
        info.showAndWait();
    }

    @FXML
    public void handleQuickStatusDelivering() {
        cbOrderStatus.setValue(OrderHistoryStore.STATUS_DELIVERING);
        handleUpdateOrderStatus();
    }

    @FXML
    public void handleQuickStatusCompleted() {
        cbOrderStatus.setValue(OrderHistoryStore.STATUS_COMPLETED);
        handleUpdateOrderStatus();
    }

    @FXML
    public void handleQuickStatusCancelled() {
        cbOrderStatus.setValue(OrderHistoryStore.STATUS_CANCELLED);
        handleUpdateOrderStatus();
    }

    /* ================================================================
       QUẢN LÝ SẢN PHẨM
       ================================================================ */
    @FXML
    public void handleAdd() {
        Flower input = readFormData();
        if (input == null) return;
        InventoryStore.addFlower(input.getName(), input.getCategory(), input.getPrice(), input.getStock());
        reloadTableData();
        refreshDashboard();
        clearForm();
    }

    @FXML
    public void handleUpdate() {
        Flower selected = flowerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn bản ghi", "Vui lòng chọn một dòng để sửa.");
            return;
        }
        Flower input = readFormData();
        if (input == null) return;
        InventoryStore.updateFlower(selected.getId(), input.getName(), input.getCategory(), input.getPrice(), input.getStock());
        reloadTableData();
        refreshDashboard();
        clearForm();
    }

    @FXML
    public void handleDelete() {
        Flower selected = flowerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn bản ghi", "Vui lòng chọn một dòng để xoá.");
            return;
        }
        InventoryStore.removeFlower(selected.getId());
        reloadTableData();
        refreshDashboard();
        clearForm();
    }

    @FXML
    public void handleClear() {
        clearForm();
    }

    public void setCurrentUser(String username) {
        if (lblProfileName != null && username != null && !username.isBlank()) {
            lblProfileName.setText(UserStore.getFullName(username));
        }
    }

    @FXML
    public void handleLogout() {
        ButtonType yesButton = new ButtonType("Có", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Không", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "", yesButton, noButton);
        confirm.setTitle("Xác nhận thao tác");
        confirm.setHeaderText("Đăng xuất khỏi hệ thống?");
        confirm.setContentText("Bạn có chắc muốn đăng xuất và quay lại trang cửa hàng?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != yesButton) return;

        if (app == null) {
            showAlert("Lỗi hệ thống", "Không thể quay lại màn hình đăng nhập.");
            return;
        }
        try {
            app.showShopScene();
        } catch (Exception ex) {
            showAlert("Lỗi hệ thống", "Không thể quay lại trang cửa hàng.");
        }
    }

    /* ================================================================
       HÀM HỖ TRỢ
       ================================================================ */
    private Flower readFormData() {
        String name = txtName.getText().trim();
        String category = cbCategory.getValue() == null ? "" : cbCategory.getValue().trim();
        String priceText = txtPrice.getText().trim();
        String stockText = txtStock.getText().trim();

        if (name.isEmpty() || category.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
            showAlert("Thiếu dữ liệu", "Vui lòng nhập đầy đủ tên, loại, giá và tồn kho.");
            return null;
        }
        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);
            if (price < 0 || stock < 0) {
                showAlert("Dữ liệu không hợp lệ", "Giá và tồn kho phải lớn hơn hoặc bằng 0.");
                return null;
            }
            return new Flower(0, name, category, price, stock);
        } catch (NumberFormatException ex) {
            showAlert("Sai định dạng", "Giá phải là số thực và tồn kho phải là số nguyên.");
            return null;
        }
    }

    private void fillForm(Flower flower) {
        if (flower == null) return;
        txtName.setText(flower.getName());
        cbCategory.setValue(flower.getCategory());
        txtPrice.setText(String.format("%.2f", flower.getPrice()));
        txtStock.setText(String.valueOf(flower.getStock()));
    }

    private void clearForm() {
        txtName.clear();
        cbCategory.getSelectionModel().selectFirst();
        txtPrice.clear();
        txtStock.clear();
        flowerTable.getSelectionModel().clearSelection();
    }

    private void reloadTableData() {
        flowers.setAll(InventoryStore.getAllFlowers());
    }

    private String fmtVnd(double value) {
        return VND.format(value) + "₫";
    }

    private void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(ALERT_TITLE);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}