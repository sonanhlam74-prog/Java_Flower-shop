package com.Connect;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public final class DatabaseSetup {

    private static final String PARAMS = "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";

    private DatabaseSetup() {}

    /** Seed 24 sản phẩm mặc định (idempotent: chỉ thêm sản phẩm còn thiếu). */
    private static int seedProducts(Connection c) throws SQLException {
        // Dữ liệu seed: {tên, danh mục, giá, tồn kho}
        Object[][] seeds = {
            {"Hoa Mai Vàng",        "Hoa tết",         280000, 30},
            {"Hoa Đào Nhật Tân",    "Hoa tết",         320000, 28},
            {"Hoa Cúc Mâm Xôi",    "Hoa tết",         260000, 24},
            {"Hoa Lan Hồ Điệp Tết","Hoa tết",         450000, 16},
            {"Hoa Trạng Nguyên",    "Hoa tết",         240000, 26},
            {"Hoa Cúc Trắng",       "Hoa Chia buồn",   190000, 25},
            {"Hoa Huệ Trắng",       "Hoa Chia buồn",   210000, 22},
            {"Hoa Ly Trắng",        "Hoa Chia buồn",   240000, 18},
            {"Hoa Hồng Trắng",      "Hoa Chia buồn",   220000, 20},
            {"Hoa Hướng Dương",     "Hoa Chúc Mừng",   230000, 26},
            {"Hoa Hồng Đỏ",         "Hoa Chúc Mừng",   250000, 32},
            {"Hoa Tulip",            "Hoa Chúc Mừng",   310000, 15},
            {"Hoa Cẩm Chướng",      "Hoa Chúc Mừng",   205000, 27},
            {"Hoa Sen Hồng",        "Hoa Chúc Mừng",   265000, 21},
            {"Hoa Hồng Phấn",       "Hoa Sinh Nhật",   275000, 20},
            {"Hoa Đồng Tiền",       "Hoa Sinh Nhật",   195000, 30},
            {"Hoa Baby Trắng",      "Hoa Sinh Nhật",   180000, 35},
            {"Hoa Lan Mokara",      "Hoa Sinh Nhật",   350000, 18},
            {"Hoa Cẩm Tú Cầu",      "Hoa Sinh Nhật",   295000, 19},
            {"Hoa Hồng Pastel",     "Hoa Cưới",        380000, 14},
            {"Hoa Cát Tường",       "Hoa Cưới",        290000, 22},
            {"Hoa Mẫu Đơn",         "Hoa Cưới",        420000, 12},
            {"Hoa Phi Yến",         "Hoa Cưới",        340000, 16},
            {"Hoa Lan Trắng Cưới",  "Hoa Cưới",        360000, 13}
        };

        String insertCat = "INSERT IGNORE INTO Categories (category_name) VALUES (?)";
        String selectCat = "SELECT category_id FROM Categories WHERE category_name = ?";
        String existsProd = "SELECT 1 FROM Products WHERE product_name = ? LIMIT 1";
        String insertProd = "INSERT INTO Products (product_name, category_id, price, stock_quantity) VALUES (?, ?, ?, ?)";
        int inserted = 0;

        for (Object[] row : seeds) {
            String catName = (String) row[1];
            // Đảm bảo danh mục tồn tại
            try (PreparedStatement ps = c.prepareStatement(insertCat)) {
                ps.setString(1, catName);
                ps.executeUpdate();
            }
            // Lấy category_id
            int catId = -1;
            try (PreparedStatement ps = c.prepareStatement(selectCat)) {
                ps.setString(1, catName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) catId = rs.getInt(1);
                }
            }
            // Bỏ qua nếu sản phẩm đã tồn tại
            boolean exists = false;
            try (PreparedStatement ps = c.prepareStatement(existsProd)) {
                ps.setString(1, (String) row[0]);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            if (exists) {
                continue;
            }
            // Thêm sản phẩm
            try (PreparedStatement ps = c.prepareStatement(insertProd)) {
                ps.setString(1, (String) row[0]);
                ps.setInt(2, catId);
                ps.setBigDecimal(3, new java.math.BigDecimal(((Number) row[2]).intValue()));
                ps.setInt(4, ((Number) row[3]).intValue());
                ps.executeUpdate();
                inserted++;
            }
        }
        return inserted;
    }

    public static void initialize() {
        String dbName = DatabaseConfig.ENV_TEST.equals(DatabaseConfig.getEnv())
                ? DatabaseConfig.DB_TEST : DatabaseConfig.DB_PROD;
        String user   = DatabaseConfig.getUser();
        String pass   = DatabaseConfig.getPassword();

        // tạo db không chỉ định DB
        String rootUrl = "jdbc:mysql://localhost:3306/" + PARAMS;
        try (Connection c = DriverManager.getConnection(rootUrl, user, pass);
             Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS `" + dbName +
                "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            System.err.println("[Setup] Không thể tạo database: " + e.getMessage());
            System.err.println("[Setup] Kiểm tra MySQL đang chạy và credentials đúng (user=" + user + ", password=" + pass + ").");
            return;
        }

        // tạo bảng và seed admin
        String dbUrl = "jdbc:mysql://localhost:3306/" + dbName + PARAMS;
        try (Connection c = DriverManager.getConnection(dbUrl, user, pass);
             Statement st = c.createStatement()) {

            // Tạo bảng Users
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Users (" +
                "  id            INT AUTO_INCREMENT PRIMARY KEY," +
                "  username      VARCHAR(50)  NOT NULL UNIQUE," +
                "  password_hash VARCHAR(255) NOT NULL," +
                "  full_name     VARCHAR(100) NOT NULL," +
                "  email         VARCHAR(100) NOT NULL," +
                "  role          VARCHAR(20)  NOT NULL DEFAULT 'customer'," +
                "  total_spent   DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                "  avatar_path   VARCHAR(500) NULL DEFAULT NULL," +
                "  created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // Migration: thêm total_spent cho DB cũ (MySQL: không dùng IF NOT EXISTS)
            try {
                st.executeUpdate(
                    "ALTER TABLE Users ADD COLUMN " +
                    "total_spent DECIMAL(12,2) NOT NULL DEFAULT 0.00"
                );
            } catch (SQLException e) {
                if (e.getErrorCode() != 1060) { // 1060 = Duplicate column
                    System.err.println("[Setup] total_spent migration: " + e.getMessage());
                }
            }

            // Migration: thêm avatar_path cho DB cũ
            try {
                st.executeUpdate(
                    "ALTER TABLE Users ADD COLUMN " +
                    "avatar_path VARCHAR(500) NULL DEFAULT NULL"
                );
            } catch (SQLException e) {
                if (e.getErrorCode() != 1060) { // 1060 = Duplicate column
                    System.err.println("[Setup] avatar_path migration: " + e.getMessage());
                }
            }

            // Seed tài khoản admin mặc định nếu chưa có
            try (ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM Users WHERE username = 'admin'")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String hash = BCrypt.hashpw("123456", BCrypt.gensalt());
                    st.executeUpdate(
                        "INSERT INTO Users (username, password_hash, full_name, email, role) VALUES (" +
                        "'admin', '" + hash + "', 'Quản trị viên', 'admin@flowershop.vn', 'admin')"
                    );
                    System.out.println("[Setup] Đã tạo tài khoản admin mặc định  →  admin / 123456");
                }
            }

            // Tạo bảng Categories
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Categories (" +
                "  category_id   INT AUTO_INCREMENT PRIMARY KEY," +
                "  category_name VARCHAR(100) NOT NULL UNIQUE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // Tạo bảng Products
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Products (" +
                "  product_id     INT AUTO_INCREMENT PRIMARY KEY," +
                "  product_name   VARCHAR(200) NOT NULL," +
                "  category_id    INT," +
                "  price          DECIMAL(15,2) NOT NULL DEFAULT 0," +
                "  stock_quantity INT NOT NULL DEFAULT 0," +
                "  CONSTRAINT chk_stock CHECK (stock_quantity >= 0)," +
                "  FOREIGN KEY (category_id) REFERENCES Categories(category_id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // Seed bổ sung để luôn đảm bảo đủ bộ 24 sản phẩm mặc định
            int inserted = seedProducts(c);
            if (inserted > 0) {
                System.out.println("[Setup] Đã seed bổ sung " + inserted + " sản phẩm mặc định vào database.");
            }

            // Tạo bảng GuestSpending — lưu tích lũy của khách theo địa chỉ IP
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS GuestSpending (" +
                "  ip          VARCHAR(45)   NOT NULL PRIMARY KEY," +
                "  total_spent DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                "  last_seen   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // Tạo bảng Orders — lịch sử đơn hàng (user_id NULL = khách vãng lai)
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Orders (" +
                "  order_id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id          INT NULL," +
                "  guest_ip         VARCHAR(45) NULL," +
                "  customer_name    VARCHAR(100) NOT NULL DEFAULT 'Khách vãng lai'," +
                "  order_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  subtotal         DECIMAL(15,2) NOT NULL DEFAULT 0," +
                "  tax              DECIMAL(15,2) NOT NULL DEFAULT 0," +
                "  discount         DECIMAL(15,2) NOT NULL DEFAULT 0," +
                "  total_amount     DECIMAL(15,2) NOT NULL DEFAULT 0," +
                "  tier_at_purchase VARCHAR(30)   NOT NULL DEFAULT 'Đồng'," +
                "  order_status     VARCHAR(30)   NOT NULL DEFAULT 'Đang giao'," +
                "  FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // Tạo bảng OrderDetails — chi tiết từng sản phẩm trong đơn hàng
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS OrderDetails (" +
                "  detail_id    INT AUTO_INCREMENT PRIMARY KEY," +
                "  order_id     INT NOT NULL," +
                "  product_name VARCHAR(200) NOT NULL," +
                "  emoji        VARCHAR(20)  NOT NULL DEFAULT ''," +
                "  quantity     INT          NOT NULL DEFAULT 1," +
                "  unit_price   DECIMAL(15,2) NOT NULL DEFAULT 0," +
                "  FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            System.out.println("[Setup] Database `" + dbName + "` ready.....");

        } catch (SQLException e) {
            System.err.println("[Setup] Lỗi khởi tạo bảng/seed: " + e.getMessage());
        }
    }
}
