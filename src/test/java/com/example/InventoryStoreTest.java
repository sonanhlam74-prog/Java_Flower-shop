package com.example;

import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 1 — Unit tests cho InventoryStore.
 * - Không cần DB
 * - Kiểm tra quản lý tồn kho: consumeStock, releaseStock, getStock
 *
 * mvn test  (unit tests chạy mặc định)
 */
@Tag("unit")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryStoreTest {

    private static final String FLOWER = "Hoa Hồng Đỏ"; // stock=32 theo seed

    @AfterEach
    void restoreStock() {
        // Trả lại stock về mức ban đầu (32) nếu đã consume trong test
        int current = InventoryStore.getStock(FLOWER);
        if (current < 32) {
            InventoryStore.releaseStock(FLOWER, 32 - current);
        }
    }

    // ─── getStock ────────────────────────────────────────────────────────────

    @Test @Order(1)
    void getStock_existingFlower_returnsPositive() {
        assertTrue(InventoryStore.getStock(FLOWER) > 0);
    }

    @Test @Order(2)
    void getStock_unknownFlower_returnsZero() {
        assertEquals(0, InventoryStore.getStock("Hoa Không Có"));
    }

    // ─── consumeStock ────────────────────────────────────────────────────────

    @Test @Order(3)
    void consumeStock_validQuantity_returnsTrue() {
        assertTrue(InventoryStore.consumeStock(FLOWER, 1));
    }

    @Test @Order(4)
    void consumeStock_decreasesStock() {
        int before = InventoryStore.getStock(FLOWER);
        InventoryStore.consumeStock(FLOWER, 3);
        assertEquals(before - 3, InventoryStore.getStock(FLOWER));
    }

    @Test @Order(5)
    void consumeStock_moreThanAvailable_returnsFalse() {
        int stock = InventoryStore.getStock(FLOWER);
        assertFalse(InventoryStore.consumeStock(FLOWER, stock + 1));
    }

    @Test @Order(6)
    void consumeStock_moreThanAvailable_stockUnchanged() {
        int before = InventoryStore.getStock(FLOWER);
        InventoryStore.consumeStock(FLOWER, before + 100);
        assertEquals(before, InventoryStore.getStock(FLOWER));
    }

    @Test @Order(7)
    void consumeStock_zeroQuantity_returnsTrue() {
        // Zero is a no-op — should succeed
        assertTrue(InventoryStore.consumeStock(FLOWER, 0));
    }

    @Test @Order(8)
    void consumeStock_unknownFlower_returnsFalse() {
        assertFalse(InventoryStore.consumeStock("Hoa Không Có", 1));
    }

    // ─── releaseStock ────────────────────────────────────────────────────────

    @Test @Order(9)
    void releaseStock_increasesStock() {
        InventoryStore.consumeStock(FLOWER, 5);
        int after = InventoryStore.getStock(FLOWER);
        InventoryStore.releaseStock(FLOWER, 5);
        assertEquals(after + 5, InventoryStore.getStock(FLOWER));
    }

    @Test @Order(10)
    void releaseStock_zeroQuantity_stockUnchanged() {
        int before = InventoryStore.getStock(FLOWER);
        InventoryStore.releaseStock(FLOWER, 0);
        assertEquals(before, InventoryStore.getStock(FLOWER));
    }

    @Test @Order(11)
    void releaseStock_unknownFlower_noException() {
        // Should silently ignore unknown flower
        assertDoesNotThrow(() -> InventoryStore.releaseStock("Hoa Không Có", 5));
    }

    // ─── consumeAndRelease cycle ─────────────────────────────────────────────

    @Test @Order(12)
    void consumeThenRelease_stockRestored() {
        int original = InventoryStore.getStock(FLOWER);
        InventoryStore.consumeStock(FLOWER, 10);
        InventoryStore.releaseStock(FLOWER, 10);
        assertEquals(original, InventoryStore.getStock(FLOWER));
    }

    // ─── getAllFlowers / addFlower / updateFlower / removeFlower ─────────────

    @Test @Order(13)
    void getAllFlowers_returnsNonEmptyList() {
        assertFalse(InventoryStore.getAllFlowers().isEmpty());
    }

    @Test @Order(14)
    void addFlower_appearsInGetAll() {
        int before = InventoryStore.getAllFlowers().size();
        String uniqueName = "Hoa Test " + UUID.randomUUID();
        Flower created = InventoryStore.addFlower(uniqueName, "Hoa Chúc Mừng", 100_000, 5);
        try {
            assertEquals(before + 1, InventoryStore.getAllFlowers().size());
            assertNotNull(InventoryStore.findFlowerByName(uniqueName));
        } finally {
            // Dọn dữ liệu test để không làm bẩn DB khi chạy nhiều lần.
            InventoryStore.removeFlower(created.getId());
        }
    }

    @Test @Order(15)
    void findFlowerByName_knownFlower_returnsFlower() {
        assertNotNull(InventoryStore.findFlowerByName(FLOWER));
    }

    @Test @Order(16)
    void findFlowerByName_unknownFlower_returnsNull() {
        assertNull(InventoryStore.findFlowerByName("Không Tồn Tại"));
    }

    @Test @Order(17)
    void normalizeCategory_variousInputs_mapsCorrectly() {
        assertEquals("Hoa tết",       InventoryStore.normalizeCategory("Hoa tết"));
        assertEquals("Hoa Chia buồn", InventoryStore.normalizeCategory("chia buồn"));
        assertEquals("Hoa Sinh Nhật", InventoryStore.normalizeCategory("sinh nhật"));
        assertEquals("Hoa Cưới",      InventoryStore.normalizeCategory("cưới"));
        assertEquals("Hoa Chúc Mừng", InventoryStore.normalizeCategory("chúc mừng"));
        assertEquals("Hoa Chúc Mừng", InventoryStore.normalizeCategory(null));
    }
}
