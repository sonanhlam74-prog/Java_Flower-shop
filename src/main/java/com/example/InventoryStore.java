package com.example;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static int nextId = 1001;

    static {
        addSeed("Hoa Mai Vàng", "Hoa tết", 280000, 30);
        addSeed("Hoa Đào Nhật Tân", "Hoa tết", 320000, 28);
        addSeed("Hoa Cúc Mâm Xôi", "Hoa tết", 260000, 24);
        addSeed("Hoa Lan Hồ Điệp Tết", "Hoa tết", 450000, 16);

        addSeed("Hoa Cúc Trắng", "Hoa Chia buồn", 190000, 25);
        addSeed("Hoa Huệ Trắng", "Hoa Chia buồn", 210000, 22);
        addSeed("Hoa Ly Trắng", "Hoa Chia buồn", 240000, 18);
        addSeed("Hoa Hồng Trắng", "Hoa Chia buồn", 220000, 20);

        addSeed("Hoa Hướng Dương", "Hoa Chúc Mừng", 230000, 26);
        addSeed("Hoa Hồng Đỏ", "Hoa Chúc Mừng", 250000, 32);
        addSeed("Hoa Tulip", "Hoa Chúc Mừng", 310000, 15);
        addSeed("Hoa Cẩm Chướng", "Hoa Chúc Mừng", 205000, 27);

        addSeed("Hoa Hồng Phấn", "Hoa Sinh Nhật", 275000, 20);
        addSeed("Hoa Đồng Tiền", "Hoa Sinh Nhật", 195000, 30);
        addSeed("Hoa Baby Trắng", "Hoa Sinh Nhật", 180000, 35);
        addSeed("Hoa Lan Mokara", "Hoa Sinh Nhật", 350000, 18);

        addSeed("Hoa Hồng Pastel", "Hoa Cưới", 380000, 14);
        addSeed("Hoa Cát Tường", "Hoa Cưới", 290000, 22);
        addSeed("Hoa Mẫu Đơn", "Hoa Cưới", 420000, 12);
        addSeed("Hoa Phi Yến", "Hoa Cưới", 340000, 16);
    }

    private InventoryStore() {
    }

    private static void addSeed(String name, String category, double price, int stock) {
        int id = nextId++;
        ITEMS.put(id, new Item(id, name, normalizeCategory(category), price, stock));
    }

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
        int id = nextId++;
        Item item = new Item(id, name, normalizeCategory(category), price, Math.max(0, stock));
        ITEMS.put(id, item);
        return new Flower(item.id, item.name, item.category, item.price, item.stock);
    }

    public static synchronized boolean updateFlower(int id, String name, String category, double price, int stock) {
        Item item = ITEMS.get(id);
        if (item == null) {
            return false;
        }
        item.name = name;
        item.category = normalizeCategory(category);
        item.price = price;
        item.stock = Math.max(0, stock);
        return true;
    }

    public static synchronized boolean removeFlower(int id) {
        return ITEMS.remove(id) != null;
    }

    public static synchronized boolean consumeStock(String flowerName, int quantity) {
        if (quantity <= 0) {
            return true;
        }
        Item item = findByName(flowerName);
        if (item == null || item.stock < quantity) {
            return false;
        }
        item.stock -= quantity;
        return true;
    }

    public static synchronized void releaseStock(String flowerName, int quantity) {
        if (quantity <= 0) {
            return;
        }
        Item item = findByName(flowerName);
        if (item == null) {
            return;
        }
        item.stock += quantity;
    }

    public static synchronized int getStock(String flowerName) {
        Item item = findByName(flowerName);
        if (item == null) {
            return 0;
        }
        return item.stock;
    }

    public static String normalizeCategory(String rawCategory) {
        if (rawCategory == null) {
            return "Hoa Chúc Mừng";
        }

        String category = rawCategory.trim().toLowerCase();
        if (category.contains("tet") || category.contains("tết")) {
            return "Hoa tết";
        }
        if (category.contains("chia") || category.contains("buon") || category.contains("buồn")) {
            return "Hoa Chia buồn";
        }
        if (category.contains("chuc") || category.contains("chúc") || category.contains("mung") || category.contains("mừng")) {
            return "Hoa Chúc Mừng";
        }
        if (category.contains("sinh") || category.contains("nhật") || category.contains("birthday")) {
            return "Hoa Sinh Nhật";
        }
        if (category.contains("cuoi") || category.contains("cưới") || category.contains("wedding")) {
            return "Hoa Cưới";
        }

        return "Hoa Chúc Mừng";
    }

    private static Item findByName(String name) {
        for (Item item : ITEMS.values()) {
            if (item.name.equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }
}