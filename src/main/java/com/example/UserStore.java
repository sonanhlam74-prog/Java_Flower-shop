package com.example;

import java.util.LinkedHashMap;
import java.util.Map;
/**
 * Kho người dùng in-memory đơn giản cho xác thực.
 * Hỗ trợ đăng ký, đăng nhập, xác minh email và đặt lại mật khẩu.
 */
public final class UserStore {

    private static final class User {
        private String password;
        private String fullName;
        private final String email;

        private User(String password, String fullName, String email) {
            this.password = password;
            this.fullName = fullName;
            this.email = email;
        }
    }

    private static final Map<String, User> USERS = new LinkedHashMap<>();

    static {
        // Tạo sẵn tài khoản admin mặc định
        USERS.put("admin", new User("123456", "Nguyễn Admin", "admin@flowershop.vn"));

    }   

    private UserStore() {
    }

    public static synchronized boolean authenticate(String username, String password) {
        User user = USERS.get(username);
        return user != null && user.password.equals(password);
    }

    public static synchronized boolean userExists(String username) {
        return USERS.containsKey(username);
    }

    public static synchronized void register(String username, String password, String fullName, String email) {
        USERS.put(username, new User(password, fullName, email));
    }

    public static synchronized boolean verifyEmail(String username, String email) {
        User user = USERS.get(username);
        return user != null && user.email.equalsIgnoreCase(email.trim());
    }

    public static synchronized void changePassword(String username, String newPassword) {
        User user = USERS.get(username);
        if (user != null) {
            user.password = newPassword;
        }
    }

    public static synchronized String getFullName(String username) {
        User user = USERS.get(username);
        return user != null ? user.fullName : username;
    }

    public static synchronized String getEmail(String username) {
        User user = USERS.get(username);
        return user != null ? user.email : "";
    }

    public static synchronized void updateFullName(String username, String newName) {
        User user = USERS.get(username);
        if (user != null) {
            user.fullName = newName;
        }
    }
}
