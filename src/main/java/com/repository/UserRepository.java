package com.repository;

/**
 * Interface định nghĩa các thao tác với dữ liệu User.
 * UserDAO (DB) và các implementation khác phải tuân theo contract này.
 */
public interface UserRepository {
    boolean authenticate(String username, String password);
    boolean userExists(String username);
    boolean register(String username, String password, String fullName, String email);
    boolean verifyEmail(String username, String email);
    boolean changePassword(String username, String newPassword);
    String getFullName(String username);
    String getEmail(String username);
    String getRole(String username);
    void updateFullName(String username, String newName);
    double getTotalSpent(String username);
    void addToTotalSpent(String username, double amount);
    String getAvatarPath(String username);
    void updateAvatarPath(String username, String path);
}
