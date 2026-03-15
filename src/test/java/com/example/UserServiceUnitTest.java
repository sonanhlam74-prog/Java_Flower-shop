package com.example;

import com.service.UserService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Level 1 — Unit tests cho UserService (bên thứ ba).
 * - Không cần DB, không cần BCrypt
 * - Mỗi test chạy < 10ms
 * - Kiểm tra logic của service layer, không phải DAO
 *
 * mvn test  (unit tests chạy mặc định, không cần DB)
 */
@Tag("unit")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceUnitTest {

    private InMemoryUserRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryUserRepository();
        UserService.setRepository(repo);
    }

    @AfterEach
    void tearDown() {
        repo.clear();
        UserService.resetInstance();
    }

    // ─── authenticate ────────────────────────────────────────────────────────

    @Test @Order(1)
    void authenticate_correctPassword_returnsTrue() {
        repo.register("alice", "pass123", "Alice", "alice@test.com");
        assertTrue(UserService.getInstance().authenticate("alice", "pass123"));
    }

    @Test @Order(2)
    void authenticate_wrongPassword_returnsFalse() {
        repo.register("alice", "pass123", "Alice", "alice@test.com");
        assertFalse(UserService.getInstance().authenticate("alice", "wrong"));
    }

    @Test @Order(3)
    void authenticate_unknownUser_returnsFalse() {
        assertFalse(UserService.getInstance().authenticate("nobody", "pass"));
    }

    @Test @Order(4)
    void authenticate_emptyPassword_returnsFalse() {
        repo.register("alice", "pass123", "Alice", "alice@test.com");
        assertFalse(UserService.getInstance().authenticate("alice", ""));
    }

    // ─── register / userExists ───────────────────────────────────────────────

    @Test @Order(5)
    void register_newUser_existsAfterwards() {
        assertFalse(UserService.getInstance().userExists("bob"));
        UserService.getInstance().register("bob", "pw", "Bob", "bob@test.com");
        assertTrue(UserService.getInstance().userExists("bob"));
    }

    @Test @Order(6)
    void register_thenAuthenticate_succeeds() {
        UserService.getInstance().register("carol", "secret", "Carol", "carol@test.com");
        assertTrue(UserService.getInstance().authenticate("carol", "secret"));
    }

    // ─── verifyEmail ─────────────────────────────────────────────────────────

    @Test @Order(7)
    void verifyEmail_correctEmail_returnsTrue() {
        repo.register("dan", "pw", "Dan", "dan@test.com");
        assertTrue(UserService.getInstance().verifyEmail("dan", "dan@test.com"));
    }

    @Test @Order(8)
    void verifyEmail_caseInsensitive_returnsTrue() {
        repo.register("dan", "pw", "Dan", "dan@test.com");
        assertTrue(UserService.getInstance().verifyEmail("dan", "DAN@TEST.COM"));
    }

    @Test @Order(9)
    void verifyEmail_wrongEmail_returnsFalse() {
        repo.register("dan", "pw", "Dan", "dan@test.com");
        assertFalse(UserService.getInstance().verifyEmail("dan", "wrong@test.com"));
    }

    // ─── changePassword ──────────────────────────────────────────────────────

    @Test @Order(10)
    void changePassword_oldPasswordNoLongerWorks() {
        repo.register("eve", "old", "Eve", "eve@test.com");
        UserService.getInstance().changePassword("eve", "new123");
        assertFalse(UserService.getInstance().authenticate("eve", "old"));
    }

    @Test @Order(11)
    void changePassword_newPasswordWorks() {
        repo.register("eve", "old", "Eve", "eve@test.com");
        UserService.getInstance().changePassword("eve", "new123");
        assertTrue(UserService.getInstance().authenticate("eve", "new123"));
    }

    // ─── getFullName / updateFullName ────────────────────────────────────────

    @Test @Order(12)
    void getFullName_returnsRegisteredName() {
        repo.register("frank", "pw", "Frank Ocean", "f@test.com");
        assertEquals("Frank Ocean", UserService.getInstance().getFullName("frank"));
    }

    @Test @Order(13)
    void updateFullName_changesName() {
        repo.register("frank", "pw", "Frank Ocean", "f@test.com");
        UserService.getInstance().updateFullName("frank", "Frank Sinatra");
        assertEquals("Frank Sinatra", UserService.getInstance().getFullName("frank"));
    }

    @Test @Order(14)
    void getFullName_unknownUser_returnsFallback() {
        // InMemoryUserRepository returns username as fallback
        assertEquals("ghost", UserService.getInstance().getFullName("ghost"));
    }

    // ─── getEmail ────────────────────────────────────────────────────────────

    @Test @Order(15)
    void getEmail_returnsRegisteredEmail() {
        repo.register("grace", "pw", "Grace", "grace@test.com");
        assertEquals("grace@test.com", UserService.getInstance().getEmail("grace"));
    }

    @Test @Order(16)
    void getEmail_unknownUser_returnsEmpty() {
        assertEquals("", UserService.getInstance().getEmail("ghost"));
    }
}
