package com.repository;

import com.Connect.ConnectDB;
import com.example.Flower;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO — truy cập trực tiếp bảng Products + Categories trong database.
 * Các thao tác CRUD được gọi từ InventoryStore.
 */
public class ProductDAO {

    /** Lấy toàn bộ sản phẩm (JOIN danh mục). */
    public List<Flower> getAllProducts() {
        List<Flower> list = new ArrayList<>();
        String sql =
            "SELECT p.product_id, p.product_name, " +
            "       COALESCE(c.category_name, 'Hoa Chúc Mừng') AS category_name, " +
            "       p.price, p.stock_quantity " +
            "FROM Products p " +
            "LEFT JOIN Categories c ON p.category_id = c.category_id " +
            "ORDER BY p.product_id";
        try (Connection conn = ConnectDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Flower(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category_name"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getAllProducts lỗi: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm sản phẩm mới. Trả về product_id được gán bởi DB, hoặc -1 nếu lỗi.
     */
    public int addProduct(String name, String category, double price, int stock) {
        int categoryId = findOrCreateCategory(category);
        if (categoryId < 0) return -1;
        String sql = "INSERT INTO Products (product_name, category_id, price, stock_quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, categoryId);
            ps.setDouble(3, price);
            ps.setInt(4, Math.max(0, stock));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] addProduct lỗi: " + e.getMessage());
        }
        return -1;
    }

    /** Cập nhật sản phẩm theo product_id. Trả về true nếu thành công. */
    public boolean updateProduct(int id, String name, String category, double price, int stock) {
        int categoryId = findOrCreateCategory(category);
        if (categoryId < 0) return false;
        String sql = "UPDATE Products SET product_name=?, category_id=?, price=?, stock_quantity=? WHERE product_id=?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, categoryId);
            ps.setDouble(3, price);
            ps.setInt(4, Math.max(0, stock));
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ProductDAO] updateProduct lỗi: " + e.getMessage());
        }
        return false;
    }

    /** Xoá sản phẩm theo product_id. Trả về true nếu thành công. */
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM Products WHERE product_id=?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ProductDAO] deleteProduct lỗi: " + e.getMessage());
        }
        return false;
    }

    /**
     * Thay đổi tồn kho theo tên sản phẩm:
     *   delta < 0 → tiêu thụ (consumeStock), delta > 0 → trả lại (releaseStock).
     * Trả về true nếu thao tác thành công (tồn kho sau >= 0).
     */
    public boolean adjustStock(String productName, int delta) {
        // Điều kiện: stock_quantity + delta >= 0
        String sql =
            "UPDATE Products SET stock_quantity = stock_quantity + ? " +
            "WHERE product_name = ? AND stock_quantity + ? >= 0";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, productName);
            ps.setInt(3, delta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ProductDAO] adjustStock lỗi: " + e.getMessage());
        }
        return false;
    }

    /** Lấy tồn kho hiện tại của sản phẩm theo tên. */
    public int getStock(String productName) {
        String sql = "SELECT stock_quantity FROM Products WHERE product_name = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getStock lỗi: " + e.getMessage());
        }
        return 0;
    }

    // -----------------------------------------------------------------------
    // Hỗ trợ nội bộ
    // -----------------------------------------------------------------------

    /** Tìm category_id theo tên, tạo mới nếu chưa có. */
    private int findOrCreateCategory(String categoryName) {
        String selectSql = "SELECT category_id FROM Categories WHERE category_name = ?";
        String insertSql = "INSERT IGNORE INTO Categories (category_name) VALUES (?)";
        try (Connection conn = ConnectDB.getConnection()) {
            // Thử tìm trước
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, categoryName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            // Chèn nếu chưa có
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, categoryName);
                ps.executeUpdate();
            }
            // Lấy lại sau khi chèn
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, categoryName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] findOrCreateCategory lỗi: " + e.getMessage());
        }
        return -1;
    }
}
