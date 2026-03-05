package com.example;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class HistoryController {
    @FXML private VBox orderListBox;
    @FXML private Label lblSummary;
    @FXML private Label lblTotalOrders;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblCurrentTier;

    private static final NumberFormat VND = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private App app;

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void initialize() {
        refreshView();
    }

    @FXML
    public void handleBackToShop() {
        try {
            app.showShopScene();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshView() {
        List<OrderHistoryStore.Order> orders = OrderHistoryStore.getAllOrders();

        lblTotalOrders.setText(String.valueOf(OrderHistoryStore.getOrderCount()));
        lblTotalRevenue.setText(fmt(OrderHistoryStore.getTotalRevenue()));
        lblCurrentTier.setText(MembershipStore.getCurrentTier().getDisplayName());
        lblSummary.setText(MembershipStore.getSummary());

        orderListBox.getChildren().clear();

        if (orders.isEmpty()) {
            Label empty = new Label("Chưa có đơn hàng nào. Hãy mua hoa nào! 🌸");
            empty.getStyleClass().add("history-empty");
            empty.setStyle("-fx-font-size: 16px; -fx-padding: 40 0;");
            orderListBox.getChildren().add(empty);
            return;
        }

        for (OrderHistoryStore.Order order : orders) {
            VBox card = buildOrderCard(order);
            orderListBox.getChildren().add(card);
        }
    }

    private VBox buildOrderCard(OrderHistoryStore.Order order) {
        VBox card = new VBox(8);
        card.getStyleClass().add("history-order-card");

        // Hàng tiêu đề
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label orderId = new Label("Đơn #" + order.getOrderId());
        orderId.getStyleClass().add("history-order-id");
        Label date = new Label(order.getDateTime());
        date.getStyleClass().add("history-order-date");
        Label tier = new Label(order.getTierAtPurchase());
        tier.getStyleClass().add("history-order-tier");
        Label status = new Label(order.getStatus());
        status.getStyleClass().add("history-order-status");
        if (OrderHistoryStore.STATUS_DELIVERING.equals(order.getStatus())) {
            status.getStyleClass().add("history-order-status-delivering");
        } else if (OrderHistoryStore.STATUS_COMPLETED.equals(order.getStatus())) {
            status.getStyleClass().add("history-order-status-completed");
        } else if (OrderHistoryStore.STATUS_CANCELLED.equals(order.getStatus())) {
            status.getStyleClass().add("history-order-status-cancelled");
        }
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label total = new Label(fmt(order.getTotal()));
        total.getStyleClass().add("history-order-total");
        header.getChildren().addAll(orderId, date, tier, status, spacer, total);

        card.getChildren().add(header);
        card.getChildren().add(new Separator());

        // Các mục sản phẩm
        for (OrderHistoryStore.OrderItem item : order.getItems()) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            Label emoji = new Label(item.getEmoji());
            emoji.setStyle("-fx-font-size: 18px;");
            Label name = new Label(item.getName());
            name.getStyleClass().add("history-item-name");
            Label qty = new Label("x" + item.getQuantity());
            qty.getStyleClass().add("history-item-qty");
            HBox sp = new HBox();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label price = new Label(fmt(item.getSubtotal()));
            price.getStyleClass().add("history-item-price");
            row.getChildren().addAll(emoji, name, qty, sp, price);
            card.getChildren().add(row);
        }

        // Chân đơn hàng
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label items = new Label(order.getItemCount() + " sản phẩm");
        items.getStyleClass().add("history-order-date");
        HBox sp2 = new HBox();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        VBox summary = new VBox(2);
        summary.setAlignment(Pos.CENTER_RIGHT);
        if (order.getDiscount() > 0) {
            Label disc = new Label("Giảm giá: -" + fmt(order.getDiscount()));
            disc.getStyleClass().add("history-item-qty");
            disc.setStyle("-fx-text-fill: #16a34a;");
            summary.getChildren().add(disc);
        }
        Label tax = new Label("Thuế (8%): " + fmt(order.getTax()));
        tax.getStyleClass().add("history-item-qty");
        summary.getChildren().add(tax);

        footer.getChildren().addAll(items, sp2, summary);
        card.getChildren().addAll(new Separator(), footer);

        return card;
    }

    private String fmt(double amount) {
        return VND.format(amount) + "₫";
    }
}
