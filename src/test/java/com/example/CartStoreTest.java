package com.example;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 1 — Unit tests cho CartStore + InventoryStore tích hợp.
 * - Không cần DB
 * - Kiểm tra giỏ hàng: thêm, xóa, tổng tiền, hết hàng
 *
 * mvn test  (unit tests chạy mặc định)
 */
@Tag("unit")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartStoreTest {

    private static final String FLOWER = "Hoa Hướng Dương"; // đủ stock để test
    private static final String RARE   = "Hoa Lan Hồ Điệp Tết"; // stock giới hạn hơn

    @BeforeEach
    void clearCart() {
        // Dọn sạch giỏ hàng trước mỗi test, trả lại stock
        for (String name : CartStore.getCatalog().keySet()) {
            CartStore.removeAllOfItem(name);
        }
    }

    // ─── addItem ─────────────────────────────────────────────────────────────

    @Test @Order(1)
    void addItem_validFlower_cartCountIncreases() {
        assertTrue(CartStore.addItem(FLOWER));
        assertEquals(1, CartStore.getCartCount());
    }

    @Test @Order(2)
    void addItem_multipleUnits_cartCountAccumulates() {
        CartStore.addItem(FLOWER);
        CartStore.addItem(FLOWER);
        assertEquals(2, CartStore.getCartCount());
    }

    @Test @Order(3)
    void addItem_unknownFlower_returnsFalse() {
        assertFalse(CartStore.addItem("Hoa Không Tồn Tại"));
        assertEquals(0, CartStore.getCartCount());
    }

    // ─── removeItem ──────────────────────────────────────────────────────────

    @Test @Order(4)
    void removeItem_afterAdd_cartCountDecreases() {
        CartStore.addItem(FLOWER);
        CartStore.addItem(FLOWER);
        CartStore.removeItem(FLOWER);
        assertEquals(1, CartStore.getCartCount());
    }

    @Test @Order(5)
    void removeItem_emptyCart_returnsFalse() {
        assertFalse(CartStore.removeItem(FLOWER));
    }

    @Test @Order(6)
    void removeItem_restoresStock() {
        int stockBefore = InventoryStore.getStock(FLOWER);
        CartStore.addItem(FLOWER);
        CartStore.removeItem(FLOWER);
        assertEquals(stockBefore, InventoryStore.getStock(FLOWER));
    }

    // ─── removeAllOfItem ─────────────────────────────────────────────────────

    @Test @Order(7)
    void removeAllOfItem_clearsAllUnits() {
        CartStore.addItem(FLOWER);
        CartStore.addItem(FLOWER);
        CartStore.addItem(FLOWER);
        CartStore.removeAllOfItem(FLOWER);
        assertEquals(0, CartStore.getCartCount());
    }

    @Test @Order(8)
    void removeAllOfItem_restoresFullStock() {
        int stockBefore = InventoryStore.getStock(FLOWER);
        CartStore.addItem(FLOWER);
        CartStore.addItem(FLOWER);
        CartStore.removeAllOfItem(FLOWER);
        assertEquals(stockBefore, InventoryStore.getStock(FLOWER));
    }

    // ─── subtotal ────────────────────────────────────────────────────────────

    @Test @Order(9)
    void getSubtotal_emptyCart_isZero() {
        assertEquals(0.0, CartStore.getSubtotal(), 0.01);
    }

    @Test @Order(10)
    void getSubtotal_oneItem_matchesCatalogPrice() {
        CartStore.Product p = CartStore.getProduct(FLOWER);
        assertNotNull(p);
        CartStore.addItem(FLOWER);
        assertEquals(p.getPrice(), CartStore.getSubtotal(), 0.01);
    }

    @Test @Order(11)
    void getSubtotal_multipleItems_isCorrectSum() {
        CartStore.Product huong = CartStore.getProduct("Hoa Hướng Dương");
        CartStore.Product hong  = CartStore.getProduct("Hoa Hồng Đỏ");
        assertNotNull(huong); assertNotNull(hong);

        CartStore.addItem("Hoa Hướng Dương");
        CartStore.addItem("Hoa Hồng Đỏ");
        CartStore.addItem("Hoa Hồng Đỏ");

        double expected = huong.getPrice() + hong.getPrice() * 2;
        assertEquals(expected, CartStore.getSubtotal(), 0.01);
    }

    // ─── clearCartAfterPurchase ──────────────────────────────────────────────

    @Test @Order(12)
    void clearCartAfterPurchase_cartBecomesEmpty() {
        CartStore.addItem(FLOWER);
        CartStore.clearCartAfterPurchase();
        assertEquals(0, CartStore.getCartCount());
    }

    @Test @Order(13)
    void clearCartAfterPurchase_doesNotRestoreStock() {
        int stockBefore = InventoryStore.getStock(FLOWER);
        CartStore.addItem(FLOWER);
        CartStore.clearCartAfterPurchase();
        // stock đã bị tiêu thụ, không được hoàn lại sau khi mua
        assertEquals(stockBefore - 1, InventoryStore.getStock(FLOWER));
    }

    // ─── out-of-stock ────────────────────────────────────────────────────────

    @Test @Order(14)
    void addItem_drainAllStock_finalAddReturnsFalse() {
        String flower = "Hoa Mẫu Đơn"; // stock=12 theo seed data
        int stock = InventoryStore.getStock(flower);
        assertTrue(stock > 0, "Flower phải có stock > 0");

        // drain all
        for (int i = 0; i < stock; i++) {
            assertTrue(CartStore.addItem(flower), "thêm lần " + (i + 1) + " phải thành công");
        }
        // lần tiếp theo phải thất bại
        assertFalse(CartStore.addItem(flower), "thêm vượt stock phải thất bại");
    }

    // ─── getProduct / getFlowerEmoji ─────────────────────────────────────────

    @Test @Order(15)
    void getProduct_knownFlower_returnsNotNull() {
        assertNotNull(CartStore.getProduct(FLOWER));
    }

    @Test @Order(16)
    void getProduct_unknownFlower_returnsNull() {
        assertNull(CartStore.getProduct("Hoa Ảo"));
    }

    @Test @Order(17)
    void getFlowerEmoji_unknownFlower_returnsFallback() {
        assertEquals("🌸", CartStore.getFlowerEmoji("Không tồn tại"));
    }
}
