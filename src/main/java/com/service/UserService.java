package com.service;

import com.repository.UserDAO;
import com.repository.UserRepository;

/**
 * UserService — BÊN THỨ BA (lớp trung gian) giữa UI/Controller và Database.
 *
 * Kiến trúc:
 *   [Test]             [App - UI/Controller]
 *      |                       |
 *   [UserDAO]          [UserService] ← bên thứ ba
 *      |                       |
 *   [TestDB]           [UserDAO]
 *                              |
 *                        [ProductionDB]
 *
 * Lớp này để :
 * - Tránh xung đột khi thay đổi schema DB (chỉ sửa DAO, Service không đổi)
 * - Test có thể inject repository giả (mock) mà không ảnh hưởng production
 * - Controller không bao giờ gọi DB trực tiếp
 *
 * Cách dùng trong production:
 *   UserService.getInstance().authenticate(username, password)
 *
 * Cách dùng trong test (inject test repository):
 *   UserService.setRepository(new UserDAO());  // db.env=test đã được set
 */
public final class UserService {

    private static UserService instance;
    private UserRepository repository;

    private UserService(UserRepository repository) {
        this.repository = repository;
    }

    /** Trả về instance singleton với UserDAO kết nối production DB. */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService(new UserDAO());
        }
        return instance;
    }

    /**
     * Cho phép inject repository khác (dùng trong test).
     * Gọi trước khi getInstance() lần đầu, hoặc để override.
     */
    public static synchronized void setRepository(UserRepository repo) {
        if (instance == null) {
            instance = new UserService(repo);
        } else {
            instance.repository = repo;
        }
    }

    // Reset instance (dùng sau mỗi test để tránh state rò rỉ). 
    public static synchronized void resetInstance() {
        instance = null;
    }

    // Delegate tới repository

    public boolean authenticate(String username, String password) {
        return repository.authenticate(username, password);
    }

    public boolean userExists(String username) {
        return repository.userExists(username);
    }

    public boolean register(String username, String password, String fullName, String email) {
        return repository.register(username, password, fullName, email);
    }

    public boolean verifyEmail(String username, String email) {
        return repository.verifyEmail(username, email);
    }

    public boolean changePassword(String username, String newPassword) {
        return repository.changePassword(username, newPassword);
    }

    public String getFullName(String username) {
        return repository.getFullName(username);
    }

    public String getEmail(String username) {
        return repository.getEmail(username);
    }

    public void updateFullName(String username, String newName) {
        repository.updateFullName(username, newName);
    }

    public String getRole(String username) {
        return repository.getRole(username);
    }

    public double getTotalSpent(String username) {
        return repository.getTotalSpent(username);
    }

    public void addToTotalSpent(String username, double amount) {
        repository.addToTotalSpent(username, amount);
    }

    public String getAvatarPath(String username) {
        return repository.getAvatarPath(username);
    }

    public void updateAvatarPath(String username, String path) {
        repository.updateAvatarPath(username, path);
    }
}
