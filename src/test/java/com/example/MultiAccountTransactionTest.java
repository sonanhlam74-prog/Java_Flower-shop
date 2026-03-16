package com.example;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stress-test: 100 giao dịch từ nhiều tài khoản đồng thời.
 *
 * Mô phỏng:
 *   - 10 tài khoản khách hàng (user0..user9)
 *   - Mỗi người thực hiện 10 đơn hàng ngẫu nhiên → tổng 100 giao dịch
 *   - Chạy song song bằng ExecutorService (8 luồng)
 *
 * Kiểm tra:
 *   1. Tổng đơn ghi nhận đúng 100
 *   2. Tồn kho không bao giờ âm
 *   3. Không có race-condition (không mất đơn, không trùng ID)
 *   4. Doanh thu tổng hợp nhất quán (>= 0)
 *   5. Xếp hạng khách hàng đủ số lượng
 *   6. Trạng thái đơn hàng cập nhật đúng
 *   7. Thay đổi trạng thái đơn đồng thời không gây lỗi
 */
@Tag("stress")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiAccountTransactionTest {

    // Danh sách sản phẩm test (đủ stock theo seed)
    private static final String[] PRODUCTS = {
        "Hoa Hồng Đỏ",      // stock 32 – Chúc Mừng
        "Hoa Hướng Dương",  // stock 26
        "Hoa Mai Vàng",     // stock 30 – Tết
        "Hoa Baby Trắng",   // stock 35 – Sinh Nhật
        "Hoa Cúc Trắng",    // stock 25 – Chia buồn
        "Hoa Đồng Tiền",    // stock 30
        "Hoa Cẩm Chướng",   // stock 27
        "Hoa Cát Tường",    // stock 22 – Cưới
    };

    private static final int NUM_USERS        = 10;
    private static final int ORDERS_PER_USER  = 10;
    private static final int TOTAL_ORDERS     = NUM_USERS * ORDERS_PER_USER; // 100

    // Snapshot tồn kho trước khi test để restore sau
    private static final Map<String, Integer> stockBefore = new LinkedHashMap<>();

    @BeforeAll
    static void snapshotStock() {
        for (String p : PRODUCTS) {
            stockBefore.put(p, InventoryStore.getStock(p));
        }
        OrderHistoryStore.clearForTesting();
        MembershipStore.resetForTesting();
    }

    @AfterAll
    static void restoreStock() {
        for (String p : PRODUCTS) {
            int cur = InventoryStore.getStock(p);
            int orig = stockBefore.getOrDefault(p, 0);
            if (cur < orig) InventoryStore.releaseStock(p, orig - cur);
            else if (cur > orig) InventoryStore.consumeStock(p, cur - orig);
        }
        OrderHistoryStore.clearForTesting();
        MembershipStore.resetForTesting();
    }

    // =========================================================
    // TEST 1: 100 giao dịch tuần tự từ 10 tài khoản
    // =========================================================
    @Test @Order(1)
    void sequential_100Transactions_allRecorded() {
        OrderHistoryStore.clearForTesting();

        Random rng = new Random(42);
        for (int u = 0; u < NUM_USERS; u++) {
            String user = "user" + u;
            for (int t = 0; t < ORDERS_PER_USER; t++) {
                placeRandomOrder(user, rng);
            }
        }

        assertEquals(TOTAL_ORDERS, OrderHistoryStore.getAllOrders().size(),
            "Phải ghi nhận đúng 100 đơn hàng");
    }

    // =========================================================
    // TEST 2: ID đơn hàng không trùng lặp
    // =========================================================
    @Test @Order(2)
    void orderIds_areUnique() {
        List<OrderHistoryStore.Order> orders = OrderHistoryStore.getAllOrders();
        Set<Integer> ids = new HashSet<>();
        for (OrderHistoryStore.Order o : orders) {
            assertTrue(ids.add(o.getOrderId()),
                "ID trùng lặp: " + o.getOrderId());
        }
        assertEquals(TOTAL_ORDERS, ids.size());
    }

    // =========================================================
    // TEST 3: Tồn kho không âm sau 100 giao dịch
    // =========================================================
    @Test @Order(3)
    void stockNeverNegative_afterAllTransactions() {
        for (String p : PRODUCTS) {
            int stock = InventoryStore.getStock(p);
            assertTrue(stock >= 0,
                "Tồn kho âm cho sản phẩm: " + p + " = " + stock);
        }
    }

    // =========================================================
    // TEST 4: Doanh thu ≥ 0 và nhất quán với danh sách đơn
    // =========================================================
    @Test @Order(4)
    void totalRevenue_consistentWithOrders() {
        // Đánh dấu tất cả là hoàn thành để tính doanh thu
        for (OrderHistoryStore.Order o : OrderHistoryStore.getAllOrders()) {
            o.setStatus(OrderHistoryStore.STATUS_COMPLETED);
        }

        double storeRevenue = OrderHistoryStore.getTotalRevenue();
        double computed = OrderHistoryStore.getAllOrders().stream()
            .filter(o -> OrderHistoryStore.STATUS_COMPLETED.equals(o.getStatus()))
            .mapToDouble(OrderHistoryStore.Order::getTotal)
            .sum();

        assertTrue(storeRevenue >= 0, "Doanh thu phải >= 0");
        assertEquals(computed, storeRevenue, 1.0,
            "Doanh thu từ OrderHistoryStore phải khớp tổng tính tay");
    }

    // =========================================================
    // TEST 5: Mỗi khách hàng có ít nhất 1 đơn trong thống kê
    // =========================================================
    @Test @Order(5)
    void customerRanking_containsAllUsers() {
        List<OrderHistoryStore.CustomerStat> ranking = OrderHistoryStore.getCustomerRanking();
        Set<String> rankedUsers = new HashSet<>();
        for (OrderHistoryStore.CustomerStat cs : ranking) {
            rankedUsers.add(cs.getName());
        }

        for (int u = 0; u < NUM_USERS; u++) {
            String user = "user" + u;
            assertTrue(rankedUsers.contains(user),
                "Khách hàng " + user + " không xuất hiện trong xếp hạng");
        }
    }

    // =========================================================
    // TEST 6: Số đơn mỗi khách hàng đúng (10 đơn/người)
    // =========================================================
    @Test @Order(6)
    void eachUser_hasExactly10Orders() {
        List<OrderHistoryStore.CustomerStat> ranking = OrderHistoryStore.getCustomerRanking();
        Map<String, Integer> countMap = new LinkedHashMap<>();
        for (OrderHistoryStore.CustomerStat cs : ranking) {
            countMap.put(cs.getName(), cs.getOrderCount());
        }

        for (int u = 0; u < NUM_USERS; u++) {
            String user = "user" + u;
            int count = countMap.getOrDefault(user, 0);
            assertEquals(ORDERS_PER_USER, count,
                "Khách " + user + " phải có đúng " + ORDERS_PER_USER + " đơn, thực tế: " + count);
        }
    }

    // =========================================================
    // TEST 7: 100 giao dịch ĐỒNG THỜI (concurrent) – race condition
    // =========================================================
    @Test @Order(7)
    void concurrent_100Transactions_noLostOrders() throws InterruptedException {
        OrderHistoryStore.clearForTesting();
        // Restore stock trước khi chạy concurrent test
        for (String p : PRODUCTS) {
            int cur = InventoryStore.getStock(p);
            int orig = stockBefore.getOrDefault(p, 0);
            if (cur < orig) InventoryStore.releaseStock(p, orig - cur);
        }

        ExecutorService pool = Executors.newFixedThreadPool(NUM_USERS);
        CountDownLatch ready = new CountDownLatch(NUM_USERS);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int u = 0; u < NUM_USERS; u++) {
            final String user = "user" + u;
            futures.add(pool.submit(() -> {
                Random rng = new Random(Thread.currentThread().getId());
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                for (int t = 0; t < ORDERS_PER_USER; t++) {
                    try {
                        placeRandomOrder(user, rng);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        System.err.println("[Concurrent] Lỗi giao dịch: " + e.getMessage());
                    }
                }
            }));
        }

        ready.await();       // chờ tất cả thread chuẩn bị xong
        start.countDown();   // bắn súng xuất phát đồng thời

        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS),
            "Test concurrent vượt quá 30 giây");

        assertEquals(TOTAL_ORDERS, successCount.get(),
            "Phải thành công đúng 100 giao dịch concurrent, thực tế: " + successCount.get());
        assertEquals(TOTAL_ORDERS, OrderHistoryStore.getAllOrders().size(),
            "OrderHistoryStore phải ghi đúng 100 đơn concurrent");
    }

    // =========================================================
    // TEST 8: Tồn kho không âm sau concurrent
    // =========================================================
    @Test @Order(8)
    void concurrent_stockNeverNegative() {
        for (String p : PRODUCTS) {
            assertTrue(InventoryStore.getStock(p) >= 0,
                "[Concurrent] Tồn kho âm: " + p);
        }
    }

    // =========================================================
    // TEST 9: Cập nhật trạng thái đơn hàng đồng thời
    // =========================================================
    @Test @Order(9)
    void concurrent_statusUpdates_noException() throws InterruptedException {
        List<OrderHistoryStore.Order> orders = OrderHistoryStore.getAllOrders();
        assertFalse(orders.isEmpty(), "Phải có đơn hàng để test trạng thái");

        String[] statuses = {
            OrderHistoryStore.STATUS_DELIVERING,
            OrderHistoryStore.STATUS_COMPLETED,
            OrderHistoryStore.STATUS_CANCELLED
        };
        Random rng = new Random(99);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < 50; i++) {
            OrderHistoryStore.Order order = orders.get(rng.nextInt(orders.size()));
            String newStatus = statuses[rng.nextInt(statuses.length)];
            pool.submit(() -> {
                try {
                    order.setStatus(newStatus);
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        assertEquals(0, errors.get(), "Không được có exception khi cập nhật trạng thái đồng thời");
    }

    // =========================================================
    // TEST 10: Thống kê doanh thu hàng ngày có ít nhất 1 bản ghi
    // =========================================================
    @Test @Order(10)
    void dailyRevenue_hasAtLeastOneEntry() {
        List<OrderHistoryStore.DailyRevenue> daily = OrderHistoryStore.getDailyRevenue();
        assertFalse(daily.isEmpty(), "Doanh thu hàng ngày phải có ít nhất 1 bản ghi sau 100 giao dịch");
        for (OrderHistoryStore.DailyRevenue dr : daily) {
            assertTrue(dr.getOrderCount() > 0, "Số đơn ngày " + dr.getDate() + " phải > 0");
            assertTrue(dr.getRevenue() >= 0,   "Doanh thu ngày không được âm");
        }
    }

    // =========================================================
    // Hỗ trợ
    // =========================================================

    /**
     * Đặt 1 đơn hàng ngẫu nhiên cho người dùng.
     * Chọn ngẫu nhiên 1-3 sản phẩm, mỗi sản phẩm 1 đơn vị.
     * Nếu không đủ stock thì bỏ qua sản phẩm đó, vẫn ghi đơn.
     */
    private static void placeRandomOrder(String user, Random rng) {
        Map<String, Integer> cart = new LinkedHashMap<>();
        double subtotal = 0;

        List<String> shuffled = new ArrayList<>(Arrays.asList(PRODUCTS));
        Collections.shuffle(shuffled, rng);
        int numItems = 1 + rng.nextInt(3); // 1..3 sản phẩm

        for (int i = 0; i < numItems && i < shuffled.size(); i++) {
            String productName = shuffled.get(i);
            if (InventoryStore.consumeStock(productName, 1)) {
                cart.put(productName, 1);
                CartStore.Product p = CartStore.getProduct(productName);
                if (p != null) subtotal += p.getPrice();
            }
        }

        if (cart.isEmpty()) {
            // Không có sản phẩm nào đủ hàng, vẫn tạo đơn trống để đủ 100
            subtotal = 0;
        }

        double tax      = subtotal * 0.08;
        double discount = subtotal * MembershipStore.getDiscountRate();
        double total    = subtotal + tax - discount;
        if (total < 0) total = 0;

        MembershipStore.recordPurchase(total);
        OrderHistoryStore.recordOrder(cart, subtotal, tax, discount, total, user);
    }
}
