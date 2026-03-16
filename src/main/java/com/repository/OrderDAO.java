package com.repository;

import com.Connect.ConnectDB;
import com.example.CartStore;
import com.example.OrderHistoryStore;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DAO đọc/ghi lịch sử đơn hàng vào bảng Orders + OrderDetails.
 *
 * Mỗi đơn thuộc về:
 *   - user_id (FK → Users.id)  nếu khách đã đăng nhập
 *   - guest_ip                 nếu khách vãng lai
 */
public class OrderDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────── WRITE ───────────────────────────────

    /**
     * Lưu đơn hàng vào DB (Orders + OrderDetails) trong một transaction.
     * Trả về order_id do DB cấp phát, hoặc -1 nếu lỗi.
     *
     * @param username      tên tài khoản nếu đã đăng nhập, null nếu guest
     * @param guestIp       địa chỉ IP nếu guest, null nếu đã đăng nhập
     * @param customerName  tên nhập ở form checkout
     * @param cartSnapshot  product name → quantity
     */
    public int saveOrder(String username, String guestIp, String customerName,
                         Map<String, Integer> cartSnapshot,
                         double subtotal, double tax, double discount, double total,
                         String tierAtPurchase) {

        final String sqlOrder =
            "INSERT INTO Orders (user_id, guest_ip, customer_name, subtotal, tax, discount," +
            " total_amount, tier_at_purchase) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        final String sqlDetail =
            "INSERT INTO OrderDetails (order_id, product_name, emoji, quantity, unit_price)" +
            " VALUES (?, ?, ?, ?, ?)";

        Connection c = null;
        try {
            c = ConnectDB.getConnection();
            c.setAutoCommit(false);

            // Lấy user_id từ username (nếu đăng nhập)
            Integer userId = username != null ? fetchUserId(c, username) : null;

            // Chèn Orders
            int orderId;
            try (PreparedStatement ps = c.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                if (userId != null) ps.setInt(1, userId); else ps.setNull(1, Types.INTEGER);
                if (guestIp != null) ps.setString(2, guestIp); else ps.setNull(2, Types.VARCHAR);
                ps.setString(3, customerName == null || customerName.isBlank() ? "Khách vãng lai" : customerName);
                ps.setDouble(4, subtotal);
                ps.setDouble(5, tax);
                ps.setDouble(6, discount);
                ps.setDouble(7, total);
                ps.setString(8, tierAtPurchase);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) { c.rollback(); return -1; }
                    orderId = keys.getInt(1);
                }
            }

            // Chèn OrderDetails theo batch
            try (PreparedStatement ps = c.prepareStatement(sqlDetail)) {
                for (Map.Entry<String, Integer> entry : cartSnapshot.entrySet()) {
                    int qty = entry.getValue();
                    if (qty <= 0) continue;
                    String name = entry.getKey();
                    CartStore.Product p = CartStore.getProduct(name);
                    if (p == null) continue;
                    ps.setInt(1, orderId);
                    ps.setString(2, name);
                    ps.setString(3, p.getEmoji() != null ? p.getEmoji() : "");
                    ps.setInt(4, qty);
                    ps.setDouble(5, p.getPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            c.commit();
            return orderId;

        } catch (SQLException e) {
            System.err.println("[OrderDAO] saveOrder lỗi: " + e.getMessage());
            if (c != null) try { c.rollback(); } catch (SQLException ignored) {}
            return -1;
        } finally {
            if (c != null) try { c.setAutoCommit(true); c.close(); } catch (SQLException ignored) {}
        }
    }

    /** Cập nhật trạng thái đơn hàng trong DB */
    public void updateStatus(int orderId, String status) {
        final String sql = "UPDATE Orders SET order_status = ? WHERE order_id = ?";
        Connection c = null;
        try {
            c = ConnectDB.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] updateStatus lỗi: " + e.getMessage());
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignored) {}
        }
    }

    // ─────────────────────────────── READ ────────────────────────────────

    /** Tải toàn bộ đơn hàng của tài khoản đăng nhập */
    public List<OrderHistoryStore.Order> loadByUser(String username) {
        final String sql =
            "SELECT o.order_id, o.order_date, o.customer_name," +
            " o.subtotal, o.tax, o.discount, o.total_amount," +
            " o.tier_at_purchase, o.order_status," +
            " od.product_name, od.emoji, od.quantity, od.unit_price" +
            " FROM Orders o" +
            " JOIN Users u ON o.user_id = u.id" +
            " LEFT JOIN OrderDetails od ON o.order_id = od.order_id" +
            " WHERE u.username = ?" +
            " ORDER BY o.order_date DESC";
        return fetchOrders(sql, username);
    }

    /** Tải toàn bộ đơn hàng của khách vãng lai theo IP */
    public List<OrderHistoryStore.Order> loadByGuestIp(String ip) {
        final String sql =
            "SELECT o.order_id, o.order_date, o.customer_name," +
            " o.subtotal, o.tax, o.discount, o.total_amount," +
            " o.tier_at_purchase, o.order_status," +
            " od.product_name, od.emoji, od.quantity, od.unit_price" +
            " FROM Orders o" +
            " LEFT JOIN OrderDetails od ON o.order_id = od.order_id" +
            " WHERE o.guest_ip = ?" +
            " ORDER BY o.order_date DESC";
        return fetchOrders(sql, ip);
    }

    // ─────────────────────────────── HELPERS ─────────────────────────────

    private List<OrderHistoryStore.Order> fetchOrders(String sql, String param) {
        // Thu thập metadata và items theo từng order_id (giữ thứ tự)
        Map<Integer, Object[]> metaMap = new LinkedHashMap<>();
        Map<Integer, List<OrderHistoryStore.OrderItem>> itemsMap = new LinkedHashMap<>();

        Connection c = null;
        try {
            c = ConnectDB.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("order_id");
                        if (!metaMap.containsKey(id)) {
                            Timestamp ts = rs.getTimestamp("order_date");
                            LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                            metaMap.put(id, new Object[]{
                                ldt.format(FMT),
                                ldt,
                                rs.getDouble("subtotal"),
                                rs.getDouble("tax"),
                                rs.getDouble("discount"),
                                rs.getDouble("total_amount"),
                                rs.getString("tier_at_purchase"),
                                rs.getString("customer_name"),
                                rs.getString("order_status")
                            });
                            itemsMap.put(id, new ArrayList<>());
                        }
                        String pName = rs.getString("product_name");
                        if (pName != null) {
                            itemsMap.get(id).add(new OrderHistoryStore.OrderItem(
                                pName,
                                rs.getString("emoji"),
                                rs.getInt("quantity"),
                                rs.getDouble("unit_price")
                            ));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] fetchOrders lỗi: " + e.getMessage());
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignored) {}
        }

        // Xây dựng danh sách Order từ dữ liệu đã thu thập
        List<OrderHistoryStore.Order> result = new ArrayList<>();
        for (Map.Entry<Integer, Object[]> entry : metaMap.entrySet()) {
            int id = entry.getKey();
            Object[] d = entry.getValue();
            OrderHistoryStore.Order order = new OrderHistoryStore.Order(
                id,
                (String) d[0],
                (LocalDateTime) d[1],
                itemsMap.get(id),
                (Double) d[2], (Double) d[3], (Double) d[4], (Double) d[5],
                (String) d[6],
                (String) d[7]
            );
            order.setStatus((String) d[8]);
            result.add(order);
        }
        return result;
    }

    private Integer fetchUserId(Connection c, String username) throws SQLException {
        final String sql = "SELECT id FROM Users WHERE username = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }
}
