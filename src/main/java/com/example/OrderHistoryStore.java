package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lưu trữ lịch sử đơn hàng (in-memory) + thống kê.
 */
public final class OrderHistoryStore {

    public static final class OrderItem {
        private final String name;
        private final String emoji;
        private final int quantity;
        private final double unitPrice;

        public OrderItem(String name, String emoji, int quantity, double unitPrice) {
            this.name = name;
            this.emoji = emoji;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getName() { return name; }
        public String getEmoji() { return emoji; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getSubtotal() { return unitPrice * quantity; }
    }

    public static final String STATUS_DELIVERING  = "Đang giao";
    public static final String STATUS_COMPLETED   = "Đã hoàn thành";
    public static final String STATUS_CANCELLED   = "Đã huỷ";

    public static final class Order {
        private final int orderId;
        private final String dateTime;
        private final LocalDateTime timestamp;
        private final List<OrderItem> items;
        private final double subtotal;
        private final double tax;
        private final double discount;
        private final double total;
        private final String tierAtPurchase;
        private final String customerName;
        private String status;

        public Order(int orderId, String dateTime, LocalDateTime timestamp, List<OrderItem> items,
                     double subtotal, double tax, double discount, double total,
                     String tierAtPurchase, String customerName) {
            this.orderId = orderId;
            this.dateTime = dateTime;
            this.timestamp = timestamp;
            this.items = Collections.unmodifiableList(items);
            this.subtotal = subtotal;
            this.tax = tax;
            this.discount = discount;
            this.total = total;
            this.tierAtPurchase = tierAtPurchase;
            this.customerName = customerName;
            this.status = STATUS_DELIVERING;
        }

        public int getOrderId() { return orderId; }
        public String getDateTime() { return dateTime; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public List<OrderItem> getItems() { return items; }
        public double getSubtotal() { return subtotal; }
        public double getTax() { return tax; }
        public double getDiscount() { return discount; }
        public double getTotal() { return total; }
        public String getTierAtPurchase() { return tierAtPurchase; }
        public String getCustomerName() { return customerName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getItemCount() {
            int count = 0;
            for (OrderItem item : items) count += item.getQuantity();
            return count;
        }
    }

    /** DTO thống kê xếp hạng khách hàng */
    public static final class CustomerStat {
        private final String name;
        private final int orderCount;
        private final double totalSpent;
        private final int totalItems;

        public CustomerStat(String name, int orderCount, double totalSpent, int totalItems) {
            this.name = name;
            this.orderCount = orderCount;
            this.totalSpent = totalSpent;
            this.totalItems = totalItems;
        }

        public String getName() { return name; }
        public int getOrderCount() { return orderCount; }
        public double getTotalSpent() { return totalSpent; }
        public int getTotalItems() { return totalItems; }
    }

    /** DTO xếp hạng doanh số hoa */
    public static final class FlowerStat {
        private final String name;
        private final String emoji;
        private final int totalSold;
        private final double totalRevenue;

        public FlowerStat(String name, String emoji, int totalSold, double totalRevenue) {
            this.name = name;
            this.emoji = emoji;
            this.totalSold = totalSold;
            this.totalRevenue = totalRevenue;
        }

        public String getName() { return name; }
        public String getEmoji() { return emoji; }
        public int getTotalSold() { return totalSold; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    /** DTO doanh thu hàng ngày */
    public static final class DailyRevenue {
        private final String date;
        private final double revenue;
        private final int orderCount;

        public DailyRevenue(String date, double revenue, int orderCount) {
            this.date = date;
            this.revenue = revenue;
            this.orderCount = orderCount;
        }

        public String getDate() { return date; }
        public double getRevenue() { return revenue; }
        public int getOrderCount() { return orderCount; }
    }

    private static final List<Order> ORDERS = new ArrayList<>();
    private static int nextOrderId = 1;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private OrderHistoryStore() {}

    /* ======================= Ghi nhận ======================= */

    public static Order recordOrder(Map<String, Integer> cartSnapshot,
                                     double subtotal, double tax, double discount, double total,
                                     String customerName) {
        List<OrderItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cartSnapshot.entrySet()) {
            if (entry.getValue() <= 0) continue;
            CartStore.Product product = CartStore.getProduct(entry.getKey());
            if (product == null) continue;
            items.add(new OrderItem(
                product.getName(), product.getEmoji(),
                entry.getValue(), product.getPrice()
            ));
        }

        String tier = MembershipStore.getCurrentTier().getDisplayName();
        LocalDateTime now = LocalDateTime.now();
        String dateTime = now.format(FMT);
        Order order = new Order(nextOrderId++, dateTime, now, items, subtotal, tax, discount, total, tier,
                customerName == null || customerName.isBlank() ? "Khách vãng lai" : customerName);
        ORDERS.add(0, order); // mới nhất lên đầu
        return order;
    }

    /* ======================= Truy vấn cơ bản ======================= */

    public static List<Order> getAllOrders() {
        return Collections.unmodifiableList(ORDERS);
    }

    public static int getOrderCount() {
        int count = 0;
        for (Order o : ORDERS) {
            if (STATUS_COMPLETED.equals(o.getStatus())) count++;
        }
        return count;
    }

    public static double getTotalRevenue() {
        double sum = 0;
        for (Order o : ORDERS) {
            if (!STATUS_COMPLETED.equals(o.getStatus())) continue;
            sum += o.getTotal();
        }
        return sum;
    }

    /* ======================= Thống kê ======================= */

    /** Tổng số hoa đã bán qua tất cả đơn hàng (chỉ tính đơn hoàn thành) */
    public static int getTotalFlowersSold() {
        int count = 0;
        for (Order o : ORDERS) {
            if (!STATUS_COMPLETED.equals(o.getStatus())) continue;
            for (OrderItem item : o.getItems()) count += item.getQuantity();
        }
        return count;
    }

    /** Số khách hàng duy nhất (chỉ tính đơn hoàn thành) */
    public static int getUniqueCustomerCount() {
        Set<String> names = new HashSet<>();
        for (Order o : ORDERS) {
            if (!STATUS_COMPLETED.equals(o.getStatus())) continue;
            names.add(o.getCustomerName());
        }
        return names.size();
    }

    /** Tỷ lệ mua = đơn hoàn thành / khách duy nhất (nếu 0 thì trả về 0) */
    public static double getPurchaseRate() {
        int customers = getUniqueCustomerCount();
        if (customers == 0) return 0;
        return (double) getOrderCount() / customers;
    }

    /** Xếp hạng doanh số hoa, giảm dần theo tổng bán (chỉ tính đơn hoàn thành) */
    public static List<FlowerStat> getFlowerRanking() {
        Map<String, int[]> soldMap = new LinkedHashMap<>();    // tên -> [đã bán]
        Map<String, double[]> revMap = new LinkedHashMap<>();  // tên -> [doanh thu]
        Map<String, String> emojiMap = new LinkedHashMap<>();

        for (Order o : ORDERS) {
            if (!STATUS_COMPLETED.equals(o.getStatus())) continue;
            for (OrderItem item : o.getItems()) {
                soldMap.computeIfAbsent(item.getName(), k -> new int[]{0})[0] += item.getQuantity();
                revMap.computeIfAbsent(item.getName(), k -> new double[]{0})[0] += item.getSubtotal();
                emojiMap.putIfAbsent(item.getName(), item.getEmoji());
            }
        }

        List<FlowerStat> result = new ArrayList<>();
        for (String name : soldMap.keySet()) {
            result.add(new FlowerStat(name, emojiMap.get(name), soldMap.get(name)[0], revMap.get(name)[0]));
        }
        result.sort((a, b) -> Integer.compare(b.getTotalSold(), a.getTotalSold()));
        return result;
    }

    /** Xếp hạng chi tiêu khách hàng, giảm dần theo tổng chi (chỉ tính đơn hoàn thành) */
    public static List<CustomerStat> getCustomerRanking() {
        Map<String, double[]> spentMap = new LinkedHashMap<>();
        Map<String, int[]> orderCountMap = new LinkedHashMap<>();
        Map<String, int[]> itemCountMap = new LinkedHashMap<>();

        for (Order o : ORDERS) {
            if (!STATUS_COMPLETED.equals(o.getStatus())) continue;
            String name = o.getCustomerName();
            spentMap.computeIfAbsent(name, k -> new double[]{0})[0] += o.getTotal();
            orderCountMap.computeIfAbsent(name, k -> new int[]{0})[0] += 1;
            itemCountMap.computeIfAbsent(name, k -> new int[]{0})[0] += o.getItemCount();
        }

        List<CustomerStat> result = new ArrayList<>();
        for (String name : spentMap.keySet()) {
            result.add(new CustomerStat(name, orderCountMap.get(name)[0],
                    spentMap.get(name)[0], itemCountMap.get(name)[0]));
        }
        result.sort((a, b) -> Double.compare(b.getTotalSpent(), a.getTotalSpent()));
        return result;
    }


    /** Phân tích doanh thu theo ngày, giảm dần */
    public static List<DailyRevenue> getDailyRevenue() {
        Map<String, double[]> revMap = new LinkedHashMap<>();
        Map<String, int[]> countMap = new LinkedHashMap<>();

        for (Order o : ORDERS) {
            if (!STATUS_COMPLETED.equals(o.getStatus())) continue;
            String date = o.getTimestamp().format(DATE_FMT);
            revMap.computeIfAbsent(date, k -> new double[]{0})[0] += o.getTotal();
            countMap.computeIfAbsent(date, k -> new int[]{0})[0] += 1;
        }

        List<DailyRevenue> result = new ArrayList<>();
        for (String date : revMap.keySet()) {
            result.add(new DailyRevenue(date, revMap.get(date)[0], countMap.get(date)[0]));
        }
        return result; // đã đúng thứ tự vì ORDERS xếp mới nhất trước
    }

    /** Chi tiết: khách hàng đã mua những hoa nào */
    public static List<FlowerStat> getFlowersByCustomer(String customerName) {
        Map<String, int[]> soldMap = new LinkedHashMap<>();
        Map<String, double[]> revMap = new LinkedHashMap<>();
        Map<String, String> emojiMap = new LinkedHashMap<>();

        for (Order o : ORDERS) {
            if (!o.getCustomerName().equals(customerName)) continue;
            if (!STATUS_COMPLETED.equals(o.getStatus())) continue;
            for (OrderItem item : o.getItems()) {
                soldMap.computeIfAbsent(item.getName(), k -> new int[]{0})[0] += item.getQuantity();
                revMap.computeIfAbsent(item.getName(), k -> new double[]{0})[0] += item.getSubtotal();
                emojiMap.putIfAbsent(item.getName(), item.getEmoji());
            }
        }

        List<FlowerStat> result = new ArrayList<>();
        for (String name : soldMap.keySet()) {
            result.add(new FlowerStat(name, emojiMap.get(name), soldMap.get(name)[0], revMap.get(name)[0]));
        }
        result.sort((a, b) -> Integer.compare(b.getTotalSold(), a.getTotalSold()));
        return result;
    }
}
