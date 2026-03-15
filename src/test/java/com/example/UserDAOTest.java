package com.example;

import com.Connect.DatabaseConfig;
import com.repository.UserDAO;
import com.service.UserService;

import org.junit.jupiter.api.*;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserDAOTest — test dùng TEST DATABASE (flowershop_test), KHÔNG phải production.
 *
 * Luồng:
 *   Test → UserDAO(testConn) → flowershop_test
 *              ↓ inject vào
 *         UserService (bên thứ ba)
 *              ↓ test khẳng định service layer hoạt động đúng
 *         App sẽ dùng cùng UserService nhưng với production DB
 *
 * QUAN TRỌNG: Chạy test này cần DB "flowershop_test" tồn tại với bảng Users.
 * SQL tạo test DB:
 *   CREATE DATABASE IF NOT EXISTS flowershop_test;
 *   USE flowershop_test;
 *   CREATE TABLE IF NOT EXISTS Users (
 *     user_id     INT AUTO_INCREMENT PRIMARY KEY,
 *     username    VARCHAR(100) UNIQUE NOT NULL,
 *     password_hash VARCHAR(255) NOT NULL,
 *     full_name   VARCHAR(255),
 *     email       VARCHAR(255),
 *     role        VARCHAR(50) DEFAULT 'customer'
 *   );
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static Connection testConn;
    private static final String TEST_USER = "test_user_unit";
    private static final String TEST_PASS = "testpass123";
    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_NAME  = "Test User";

    // ─── Setup / Teardown ────────────────────────────────────────────────────

    @BeforeAll
    static void connectToTestDb() throws SQLException {
        // Kết nối trực tiếp tới flowershop_test (KHÔNG qua DatabaseConfig env)
        testConn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/flowershop_test",
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword());

        // Inject UserDAO dùng test connection vào UserService (bên thứ ba)
        UserService.setRepository(new UserDAO(testConn));

        cleanupTestUser();
    }

    @AfterEach
    void cleanupAfterEach() throws SQLException {
        cleanupTestUser();
    }

    @AfterAll
    static void closeTestDb() throws SQLException {
        cleanupTestUser();
        if (testConn != null && !testConn.isClosed()) {
            testConn.close();
        }
        // Reset để production app dùng UserDAO mặc định (production DB)
        UserService.resetInstance();
    }

    private static void cleanupTestUser() throws SQLException {
        try (PreparedStatement ps = testConn.prepareStatement(
                "DELETE FROM Users WHERE username = ?")) {
            ps.setString(1, TEST_USER);
            ps.executeUpdate();
        }
    }

    // ─── Tests (đều đi qua UserService — bên thứ ba) ───────────────────────

    @Test
    @Order(1)
    void testRegisterAndUserExists() {
        // Chưa tồn tại
        assertFalse(UserService.getInstance().userExists(TEST_USER),
                "User chưa đăng ký không được tồn tại");

        // Đăng ký qua UserService (bên thứ ba)
        UserService.getInstance().register(TEST_USER, TEST_PASS, TEST_NAME, TEST_EMAIL);

        // Xác nhận đã tồn tại
        assertTrue(UserService.getInstance().userExists(TEST_USER),
                "User đã đăng ký phải tồn tại");
    }

    @Test
    @Order(2)
    void testAuthenticate() {
        UserService.getInstance().register(TEST_USER, TEST_PASS, TEST_NAME, TEST_EMAIL);

        assertTrue(UserService.getInstance().authenticate(TEST_USER, TEST_PASS),
                "Mật khẩu đúng phải xác thực thành công");
        assertFalse(UserService.getInstance().authenticate(TEST_USER, "wrong_password"),
                "Mật khẩu sai phải xác thực thất bại");
        assertFalse(UserService.getInstance().authenticate("nonexistent", TEST_PASS),
                "User không tồn tại phải trả về false");
    }

    @Test
    @Order(3)
    void testVerifyEmail() {
        UserService.getInstance().register(TEST_USER, TEST_PASS, TEST_NAME, TEST_EMAIL);

        assertTrue(UserService.getInstance().verifyEmail(TEST_USER, TEST_EMAIL),
                "Email đúng phải xác minh thành công");
        assertFalse(UserService.getInstance().verifyEmail(TEST_USER, "wrong@example.com"),
                "Email sai phải xác minh thất bại");
    }

    @Test
    @Order(4)
    void testChangePassword() {
        UserService.getInstance().register(TEST_USER, TEST_PASS, TEST_NAME, TEST_EMAIL);

        String newPass = "newpassword456";
        UserService.getInstance().changePassword(TEST_USER, newPass);

        assertFalse(UserService.getInstance().authenticate(TEST_USER, TEST_PASS),
                "Mật khẩu cũ phải không còn hợp lệ");
        assertTrue(UserService.getInstance().authenticate(TEST_USER, newPass),
                "Mật khẩu mới phải xác thực thành công");
    }

    @Test
    @Order(5)
    void testGetAndUpdateFullName() {
        UserService.getInstance().register(TEST_USER, TEST_PASS, TEST_NAME, TEST_EMAIL);

        assertEquals(TEST_NAME, UserService.getInstance().getFullName(TEST_USER),
                "Full name ban đầu phải đúng");

        UserService.getInstance().updateFullName(TEST_USER, "Updated Name");
        assertEquals("Updated Name", UserService.getInstance().getFullName(TEST_USER),
                "Full name sau khi cập nhật phải thay đổi");
    }

    @Test
    @Order(6)
    void testGetEmail() {
        UserService.getInstance().register(TEST_USER, TEST_PASS, TEST_NAME, TEST_EMAIL);

        assertEquals(TEST_EMAIL, UserService.getInstance().getEmail(TEST_USER),
                "Email trả về phải khớp với email đăng ký");
    }
}
