package com.repository;

import com.Connect.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO lưu trữ tích lũy chi tiêu của khách vãng lai theo địa chỉ IP.
 * Sử dụng bảng GuestSpending (ip PK, total_spent, last_seen).
 */
public class GuestSpendingDAO {

    /**
     * Lấy tổng chi tiêu đã tích lũy của guest theo IP.
     * Trả về 0.0 nếu IP chưa có trong DB hoặc DB không khả dụng.
     */
    public double getTotalSpent(String ip) {
        String sql = "SELECT total_spent FROM GuestSpending WHERE ip = ?";
        Connection c = null;
        try {
            c = ConnectDB.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, ip);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble("total_spent");
                }
            }
        } catch (SQLException e) {
            System.err.println("[GuestSpendingDAO] getTotalSpent lỗi: " + e.getMessage());
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignored) {}
        }
        return 0.0;
    }

    /**
     * Cộng thêm amount vào tích lũy của guest.
     * Dùng upsert: tạo mới nếu IP chưa tồn tại, cộng dồn nếu đã có.
     */
    public void addToTotalSpent(String ip, double amount) {
        String sql = "INSERT INTO GuestSpending (ip, total_spent) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "total_spent = total_spent + VALUES(total_spent), " +
                     "last_seen = CURRENT_TIMESTAMP";
        Connection c = null;
        try {
            c = ConnectDB.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, ip);
                ps.setDouble(2, amount);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[GuestSpendingDAO] addToTotalSpent lỗi: " + e.getMessage());
        } finally {
            if (c != null) try { c.close(); } catch (SQLException ignored) {}
        }
    }
}
