package com.example;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Level 1 — Unit tests cho MembershipStore.
 * - Không cần DB
 * - Kiểm tra tất cả ngưỡng hạng, % giảm giá, số tiền còn lại
 *
 * mvn test  (unit tests chạy mặc định)
 */
@Tag("unit")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MembershipStoreTest {

    @BeforeEach
    void reset() {
        MembershipStore.resetForTesting(); // package-private helper
    }

    // ─── Tier thresholds ─────────────────────────────────────────────────────

    @Test @Order(1)
    void initialTier_isBronze() {
        assertEquals(MembershipStore.Tier.BRONZE, MembershipStore.getCurrentTier());
    }

    @Test @Order(2)
    void spend499999_stillBronze() {
        MembershipStore.recordPurchase(499_999);
        assertEquals(MembershipStore.Tier.BRONZE, MembershipStore.getCurrentTier());
    }

    @Test @Order(3)
    void spend500000_becomesSilver() {
        MembershipStore.recordPurchase(500_000);
        assertEquals(MembershipStore.Tier.SILVER, MembershipStore.getCurrentTier());
    }

    @Test @Order(4)
    void spend1500000_becomesGold() {
        MembershipStore.recordPurchase(1_500_000);
        assertEquals(MembershipStore.Tier.GOLD, MembershipStore.getCurrentTier());
    }

    @Test @Order(5)
    void spend3000000_becomesDiamond() {
        MembershipStore.recordPurchase(3_000_000);
        assertEquals(MembershipStore.Tier.DIAMOND, MembershipStore.getCurrentTier());
    }

    @Test @Order(6)
    void spend5000000_becomesVIP() {
        MembershipStore.recordPurchase(5_000_000);
        assertEquals(MembershipStore.Tier.VIP, MembershipStore.getCurrentTier());
    }

    @Test @Order(7)
    void tierUpdatesAccumulatively() {
        MembershipStore.recordPurchase(300_000);
        assertEquals(MembershipStore.Tier.BRONZE, MembershipStore.getCurrentTier());
        MembershipStore.recordPurchase(300_000); // total = 600_000
        assertEquals(MembershipStore.Tier.SILVER, MembershipStore.getCurrentTier());
    }

    // ─── Discount rates ──────────────────────────────────────────────────────

    @Test @Order(8)
    void bronzeDiscount_isZero() {
        assertEquals(0.00, MembershipStore.getDiscountRate(), 0.001);
    }

    @Test @Order(9)
    void silverDiscount_is5Percent() {
        MembershipStore.recordPurchase(500_000);
        assertEquals(0.05, MembershipStore.getDiscountRate(), 0.001);
    }

    @Test @Order(10)
    void goldDiscount_is10Percent() {
        MembershipStore.recordPurchase(1_500_000);
        assertEquals(0.10, MembershipStore.getDiscountRate(), 0.001);
    }

    @Test @Order(11)
    void diamondDiscount_is15Percent() {
        MembershipStore.recordPurchase(3_000_000);
        assertEquals(0.15, MembershipStore.getDiscountRate(), 0.001);
    }

    @Test @Order(12)
    void vipDiscount_is20Percent() {
        MembershipStore.recordPurchase(5_000_000);
        assertEquals(0.20, MembershipStore.getDiscountRate(), 0.001);
    }

    // ─── getNextTier / getAmountToNextTier ───────────────────────────────────

    @Test @Order(13)
    void bronze_nextTierIsSilver() {
        assertEquals(MembershipStore.Tier.SILVER, MembershipStore.getNextTier());
    }

    @Test @Order(14)
    void bronze_amountToNextTier_is500000() {
        assertEquals(500_000, MembershipStore.getAmountToNextTier(), 0.01);
    }

    @Test @Order(15)
    void silver_amountToNextTier_calculatedCorrectly() {
        MembershipStore.recordPurchase(700_000); // spent 700k, next is Gold at 1.5M
        assertEquals(800_000, MembershipStore.getAmountToNextTier(), 0.01);
    }

    @Test @Order(16)
    void vip_nextTierIsNull() {
        MembershipStore.recordPurchase(5_000_000);
        assertNull(MembershipStore.getNextTier());
    }

    @Test @Order(17)
    void vip_amountToNextTierIsZero() {
        MembershipStore.recordPurchase(5_000_000);
        assertEquals(0.0, MembershipStore.getAmountToNextTier(), 0.01);
    }

    // ─── getTotalSpent ───────────────────────────────────────────────────────

    @Test @Order(18)
    void getTotalSpent_accumulatesCorrectly() {
        MembershipStore.recordPurchase(100_000);
        MembershipStore.recordPurchase(200_000);
        assertEquals(300_000, MembershipStore.getTotalSpent(), 0.01);
    }

    // ─── getSummary ──────────────────────────────────────────────────────────

    @Test @Order(19)
    void getSummary_containsTierName() {
        MembershipStore.recordPurchase(500_000);
        String summary = MembershipStore.getSummary();
        assertTrue(summary.contains("Bạc"), "Summary phải chứa tên hạng Bạc: " + summary);
    }

    @Test @Order(20)
    void getSummary_vipShowsMaxTier() {
        MembershipStore.recordPurchase(5_000_000);
        String summary = MembershipStore.getSummary();
        assertTrue(summary.contains("cao nhất"), "VIP summary phải thể hiện hạng cao nhất: " + summary);
    }
}
