package com.example;

import com.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;

/**
 * UserRepository in-memory cho unit test.
 * - Không cần DB, không dùng BCrypt → mỗi test chạy < 5ms
 * - Dùng cùng package com.example để truy cập MembershipStore.resetForTesting()
 */
class InMemoryUserRepository implements UserRepository {

    // key=username, value=[password, fullName, email]
    private final Map<String, String[]> users = new HashMap<>();
    private final Map<String, Double> totalSpentMap = new HashMap<>();

    void clear() { users.clear(); totalSpentMap.clear(); }

    @Override
    public boolean authenticate(String username, String password) {
        String[] u = users.get(username);
        return u != null && u[0].equals(password);
    }

    @Override
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    @Override
    public boolean register(String username, String password, String fullName, String email) {
        users.put(username, new String[]{password, fullName, email});
        return true;
    }

    @Override
    public boolean verifyEmail(String username, String email) {
        String[] u = users.get(username);
        return u != null && u[2].equalsIgnoreCase(email.trim());
    }

    @Override
    public boolean changePassword(String username, String newPassword) {
        String[] u = users.get(username);
        if (u != null) { u[0] = newPassword; return true; }
        return false;
    }

    @Override
    public String getFullName(String username) {
        String[] u = users.get(username);
        return u != null ? u[1] : username;
    }

    @Override
    public String getEmail(String username) {
        String[] u = users.get(username);
        return u != null ? u[2] : "";
    }

    @Override
    public void updateFullName(String username, String newName) {
        String[] u = users.get(username);
        if (u != null) u[1] = newName;
    }

    @Override
    public String getRole(String username) {
        return "customer";
    }

    @Override
    public double getTotalSpent(String username) {
        return totalSpentMap.getOrDefault(username, 0.0);
    }

    @Override
    public void addToTotalSpent(String username, double amount) {
        totalSpentMap.merge(username, amount, Double::sum);
    }

    @Override
    public String getAvatarPath(String username) {
        return null;
    }

    @Override
    public void updateAvatarPath(String username, String path) {
        // no-op in memory
    }
}
