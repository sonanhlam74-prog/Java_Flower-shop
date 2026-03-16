package com.Connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Cung cấp kết nối JDBC tùy theo môi trường (DatabaseConfig).
 * - db.env=test        → flowershop_test
 * - db.env=production  → flowershop (mặc định)
 */
public class ConnectDB {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword());
    }
}
