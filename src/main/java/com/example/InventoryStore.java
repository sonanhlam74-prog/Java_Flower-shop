package com.example;

import com.repository.ProductDAO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * InventoryStore — bộ nhớ đệm in-memory kết hợp persistence qua ProductDAO.
 *
 * Luồng khởi tạo:
 *   1. Tải toàn bộ sản phẩm từ database vào ITEMS.
 *   2. Nếu DB không khả dụng (chưa kết nối / bảng chưa tồn tại),
 *      dùng dữ liệu seed in-memory như trước để app vẫn chạy được.
 *
 * Mọi thao tác thêm / sửa / xoá đều được ghi đồng thời vào DB (nếu DB mode)
 * và cập nhật cache in-memory.
 */
public final class InventoryStore {
    private static final class Item {
        private final int id;
        private String name;
        private String category;
        private double price;
        private int stock;

        private Item(int id, String name, String category, double price, int stock) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
        }
    }

    private static final Map<Integer, Item> ITEMS = new LinkedHashMap<>();
    private static final ProductDAO DAO = new ProductDAO();
    /** true nếu dữ liệu được tải từ / ghi vào database thành công */
    private static boolean dbMode = false;
    private static int nextId = 1;   // chỉ dùng khi fallback in-memory

    static {
        loadFromDb();
    }

    private InventoryStore() {}

    // -----------------------------------------------------------------------
    // Khởi tạo
    // -----------------------------------------------------------------------

    /** Tải dữ liệu từ DB vào cache. Nếu lỗi, dùng seed in-memory. */
    private static void loadFromDb() {
        try {
            List<Flower> fromDb = DAO.getAllProducts();
            if (!fromDb.isEmpty()) {
                for (Flower f : fromDb) {
                    ITEMS.put(f.getId(), new Item(f.getId(), f.getName(), f.getCategory(), f.getPrice(), f.getStock()));
                }
                dbMode = true;
                System.out.println("[InventoryStore] Imported " + ITEMS.size() + " items from database.");
                return;
            }
        } catch (Exception e) {
            System.err.println("[InventoryStore] Không thể kết nối DB, dùng dữ liệu seed: " + e.getMessage());
        }
        // Fallback: in-memory seed (DB chưa có dữ liệu hoặc không kết nối được)
        seedInMemory();
    }

    private static void seedInMemory() {
        int id = 1000;
        addSeed(++id, "Hoa Mai Vàng",         "Hoa tết",         280000, 30);
        addSeed(++id, "Hoa Đào Nhật Tân",     "Hoa tết",         320000, 28);
        addSeed(++id, "Hoa Cúc Mâm Xôi",     "Hoa tết",         260000, 24);
        addSeed(++id, "Hoa Lan Hồ Điệp Tết", "Hoa tết",         450000, 16);
        addSeed(++id, "Hoa Trạng Nguyên",     "Hoa tết",         240000, 26);
        addSeed(++id, "Hoa Cúc Trắng",        "Hoa Chia buồn",   190000, 25);
        addSeed(++id, "Hoa Huệ Trắng",        "Hoa Chia buồn",   210000, 22);
        addSeed(++id, "Hoa Ly Trắng",         "Hoa Chia buồn",   240000, 18);
        addSeed(++id, "Hoa Hồng Trắng",       "Hoa Chia buồn",   220000, 20);
        addSeed(++id, "Hoa Hướng Dương",      "Hoa Chúc Mừng",   230000, 26);
        addSeed(++id, "Hoa Hồng Đỏ",          "Hoa Chúc Mừng",   250000, 32);
        addSeed(++id, "Hoa Tulip",             "Hoa Chúc Mừng",   310000, 15);
        addSeed(++id, "Hoa Cẩm Chướng",       "Hoa Chúc Mừng",   205000, 27);
        addSeed(++id, "Hoa Sen Hồng",         "Hoa Chúc Mừng",   265000, 21);
        addSeed(++id, "Hoa Hồng Phấn",        "Hoa Sinh Nhật",   275000, 20);
        addSeed(++id, "Hoa Đồng Tiền",        "Hoa Sinh Nhật",   195000, 30);
        addSeed(++id, "Hoa Baby Trắng",       "Hoa Sinh Nhật",   180000, 35);
        addSeed(++id, "Hoa Lan Mokara",       "Hoa Sinh Nhật",   350000, 18);
        addSeed(++id, "Hoa Cẩm Tú Cầu",       "Hoa Sinh Nhật",   295000, 19);
        addSeed(++id, "Hoa Hồng Pastel",      "Hoa Cưới",        380000, 14);
        addSeed(++id, "Hoa Cát Tường",        "Hoa Cưới",        290000, 22);
        addSeed(++id, "Hoa Mẫu Đơn",          "Hoa Cưới",        420000, 12);
        addSeed(++id, "Hoa Phi Yến",           "Hoa Cưới",        340000, 16);
        addSeed(++id, "Hoa Lan Trắng Cưới",   "Hoa Cưới",        360000, 13);
        nextId = id + 1;
        System.out.println("[InventoryStore] Dùng dữ liệu seed in-memory (" + ITEMS.size() + " sản phẩm).");
    }

    private static void addSeed(int id, String name, String category, double price, int stock) {
        ITEMS.put(id, new Item(id, name, normalizeCategory(category), price, stock));
    }

    // -----------------------------------------------------------------------
    // API công khai
    // -----------------------------------------------------------------------

    public static synchronized List<Flower> getAllFlowers() {
        List<Flower> flowers = new ArrayList<>();
        for (Item item : ITEMS.values()) {
            flowers.add(new Flower(item.id, item.name, item.category, item.price, item.stock));
        }
        return flowers;
    }

    public static synchronized Flower findFlowerByName(String name) {
        Item item = findByName(name.trim());
        if (item == null) return null;
        return new Flower(item.id, item.name, item.category, item.price, item.stock);
    }

    public static synchronized Flower addFlower(String name, String category, double price, int stock) {
        String cat = normalizeCategory(category);
        int id;
        if (dbMode) {
            id = DAO.addProduct(name, cat, price, stock);
            if (id < 0) {
                System.err.println("[InventoryStore] Không thể thêm sản phẩm vào DB, dùng in-memory.");
                id = nextId++;
            }
        } else {
            id = nextId++;
        }
        Item item = new Item(id, name, cat, price, Math.max(0, stock));
        ITEMS.put(id, item);
        return new Flower(item.id, item.name, item.category, item.price, item.stock);
    }

    public static synchronized boolean updateFlower(int id, String name, String category, double price, int stock) {
        Item item = ITEMS.get(id);
        if (item == null) return false;
        String cat = normalizeCategory(category);
        if (dbMode) {
            DAO.updateProduct(id, name, cat, price, stock);
        }
        item.name     = name;
        item.category = cat;
        item.price    = price;
        item.stock    = Math.max(0, stock);
        return true;
    }

    public static synchronized boolean removeFlower(int id) {
        if (ITEMS.remove(id) == null) return false;
        if (dbMode) DAO.deleteProduct(id);
        return true;
    }

    public static synchronized boolean consumeStock(String flowerName, int quantity) {
        if (quantity <= 0) return true;
        Item item = findByName(flowerName);
        if (item == null || item.stock < quantity) return false;
        if (dbMode) {
            boolean ok = DAO.adjustStock(flowerName, -quantity);
            if (!ok) return false;
        }
        item.stock -= quantity;
        return true;
    }

    public static synchronized void releaseStock(String flowerName, int quantity) {
        if (quantity <= 0) return;
        Item item = findByName(flowerName);
        if (item == null) return;
        if (dbMode) DAO.adjustStock(flowerName, quantity);
        item.stock += quantity;
    }

    public static synchronized int getStock(String flowerName) {
        Item item = findByName(flowerName);
        return item == null ? 0 : item.stock;
    }

    // -----------------------------------------------------------------------
    // Tiện ích
    // -----------------------------------------------------------------------

    public static String normalizeCategory(String rawCategory) {
        if (rawCategory == null) return "Hoa Chúc Mừng";
        String category = rawCategory.trim().toLowerCase();
        if (category.contains("tet") || category.contains("tết"))
            return "Hoa tết";
        if (category.contains("chia") || category.contains("buon") || category.contains("buồn"))
            return "Hoa Chia buồn";
        if (category.contains("chuc") || category.contains("chúc") || category.contains("mung") || category.contains("mừng"))
            return "Hoa Chúc Mừng";
        if (category.contains("sinh") || category.contains("nhật") || category.contains("birthday"))
            return "Hoa Sinh Nhật";
        if (category.contains("cuoi") || category.contains("cưới") || category.contains("wedding"))
            return "Hoa Cưới";
        return "Hoa Chúc Mừng";
    }

    private static Item findByName(String name) {
        for (Item item : ITEMS.values()) {
            if (item.name.equalsIgnoreCase(name)) return item;
        }
        return null;
    }
}