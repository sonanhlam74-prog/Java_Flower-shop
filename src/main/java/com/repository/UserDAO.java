package com.repository;

import com.Connect.ConnectDB;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

/**
 * UserDAO — truy cập trực tiếp vào database.
 *
 * LƯU Ý: KHÔNG gọi trực tiếp từ UI/Controller.
 * Luôn đi qua UserService (bên thứ ba).
 */
public class UserDAO implements UserRepository {

    private final Connection injectedConn;

    public UserDAO() { this.injectedConn = null; }

    // Constructor cho test — inject kết nối test DB, không tự đóng. 
    public UserDAO(Connection conn) { this.injectedConn = conn; }

    private Connection openConn() throws SQLException {
        return injectedConn != null ? injectedConn : ConnectDB.getConnection();
    }

    private void closeConn(Connection c) {
        if (injectedConn == null && c != null) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public boolean authenticate(String username, String password) {
        String sql = "SELECT password_hash FROM Users WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return BCrypt.checkpw(password, rs.getString("password_hash"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] authenticate error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
        return false;
    }

    @Override
    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM Users WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] userExists error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
        return false;
    }

    @Override
    public boolean register(String username, String password, String fullName, String email) {
        String sql = "INSERT INTO Users (username, password_hash, full_name, email, role) VALUES (?, ?, ?, ?, 'customer')";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
                ps.setString(3, fullName);
                ps.setString(4, email);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] register error: " + e.getMessage());
            return false;
        } finally {
            closeConn(c);
        }
    }

    @Override
    public boolean verifyEmail(String username, String email) {
        String sql = "SELECT email FROM Users WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("email").equalsIgnoreCase(email.trim());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] verifyEmail error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
        return false;
    }

    @Override
    public boolean changePassword(String username, String newPassword) {
        String sql = "UPDATE Users SET password_hash = ? WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                ps.setString(2, username);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] changePassword error: " + e.getMessage());
            return false;
        } finally {
            closeConn(c);
        }
    }

    @Override
    public String getFullName(String username) {
        String sql = "SELECT full_name FROM Users WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("full_name");
                }
            }   
        } catch (SQLException e) {
            System.err.println("[UserDAO] getFullName error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
        return username;
    }

    @Override
    public String getEmail(String username) {
        String sql = "SELECT email FROM Users WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getEmail error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
        return "";
    }

    @Override
    public void updateFullName(String username, String newName) {
        String sql = "UPDATE Users SET full_name = ? WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, newName);
                ps.setString(2, username);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] updateFullName error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
    }

    @Override
    public String getRole(String username) {
        String sql = "SELECT role FROM Users WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getRole error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
        return "customer";
    }

    @Override
    public double getTotalSpent(String username) {
        String sql = "SELECT total_spent FROM Users WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble("total_spent");
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getTotalSpent error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
        return 0.0;
    }

    @Override
    public void addToTotalSpent(String username, double amount) {
        String sql = "UPDATE Users SET total_spent = total_spent + ? WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setDouble(1, amount);
                ps.setString(2, username);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] addToTotalSpent error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
    }

    @Override
    public String getAvatarPath(String username) {
        String sql = "SELECT avatar_path FROM Users WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("avatar_path");
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAvatarPath error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
        return null;
    }

    @Override
    public void updateAvatarPath(String username, String path) {
        String sql = "UPDATE Users SET avatar_path = ? WHERE username = ?";
        Connection c = null;
        try {
            c = openConn();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, path);
                ps.setString(2, username);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] updateAvatarPath error: " + e.getMessage());
        } finally {
            closeConn(c);
        }
    }
}
