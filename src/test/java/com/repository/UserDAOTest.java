package com.repository;

import com.Connect.ConnectDB;
import com.Connect.DatabaseConfig;
import com.service.UserService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test UserDAO với test database (flowershop_test).
 *
 * Luồng hoạt động:
 *   Test → UserDAO(testConn) → flowershop_test
 *
 * Khi test pass, app dùng luồng production:
 *   App → UserService (bên thứ ba) → UserDAO() → flowershop
 *
 * Dùng @BeforeAll để set db.env=test → ConnectDB.getConnection() trả về test DB.
 * Inject kết nối vào UserDAO để tránh mở/đóng nhiều lần.
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static Connection testConn;
    private UserDAO dao;

    @BeforeAll
    static void setUpClass() throws SQLException {
        // Chỉ định môi trường test → ConnectDB dùng flowershop_test
        System.setProperty("db.env", DatabaseConfig.ENV_TEST);
        testConn = ConnectDB.getConnection();

        // Reset UserService để tránh rò rỉ state từ production
        UserService.resetInstance();
    }

    @AfterAll
    static void tearDownClass() throws SQLException {
        System.clearProperty("db.env");
        UserService.resetInstance();
        if (testConn != null && !testConn.isClosed()) {
            testConn.close();
        }
    }

    @BeforeEach
    void setUp() {
        // Inject test connection vào DAO — không kết nối production
        dao = new UserDAO(testConn);
    }

    // Test trực tiếp qua UserDAO 
    @Test
    @Order(1)
    void testAuthenticateAdmin_shouldSucceed() {
        assertTrue(dao.authenticate("admin", "123456"),
                "Đăng nhập admin với đúng mật khẩu phải thành công");
    }

    @Test
    @Order(2)
    void testAuthenticateAdmin_wrongPassword_shouldFail() {
        assertFalse(dao.authenticate("admin", "wrongpassword"),
                "Đăng nhập với sai mật khẩu không được thành công");
    }

    @Test
    @Order(3)
    void testUserExists_admin_shouldBeTrue() {
        assertTrue(dao.userExists("admin"),
                "User admin phải tồn tại trong test DB");
    }

    @Test
    @Order(4)
    void testUserExists_unknownUser_shouldBeFalse() {
        assertFalse(dao.userExists("__nonexistent_xyz__"),
                "User không tồn tại phải trả về false");
    }

    // Test qua UserService (bên thứ ba)

    @Test
    @Order(5)
    void testViaUserService_authenticateAdmin_shouldSucceed() {
        // Inject DAO dùng test connection vào UserService (bên thứ ba)
        UserService.setRepository(dao);

        assertTrue(UserService.getInstance().authenticate("admin", "123456"),
                "UserService.authenticate qua test DB phải thành công");
    }

    @Test
    @Order(6)
    void testViaUserService_userExists_shouldBeTrue() {
        UserService.setRepository(dao);

        assertTrue(UserService.getInstance().userExists("admin"),
                "UserService.userExists qua test DB phải trả về true cho admin");
    }
}
