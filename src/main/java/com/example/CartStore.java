package com.example;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CartStore {
    public static final class Product {
        private final String name;
        private final String category;
        private final String emoji;
        private final String imageUrl;
        private final double price;

        public Product(String name, String category, String emoji, String imageUrl, double price) {
            this.name = name;
            this.category = category;
            this.emoji = emoji;
            this.imageUrl = (imageUrl == null) ? "" : imageUrl;
            this.price = price;
        }

        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getEmoji() { return emoji; }
        public String getImageUrl() { return imageUrl; }
        public double getPrice() { return price; }
    }

    private static final Map<String, Product> CATALOG = new LinkedHashMap<>();
    private static final Map<String, Integer> CART = new LinkedHashMap<>();

    /** Hỗ trợ – chuyển tài nguyên trong /com/example/photo/ thành chuỗi URL. */
    private static String photo(String filename) {
        java.net.URL url = CartStore.class.getResource("/com/example/photo/" + filename);
        return (url != null) ? url.toExternalForm() : "";
    }

    static {
        // ── Hoa Tết ──
        register(new Product("Hoa Mai Vàng",        "Hoa tết",       "🌼", photo("hoa-mai.jpg"), 280000));
        register(new Product("Hoa Đào Nhật Tân",    "Hoa tết",       "🌺", photo("dao-nhat-tan.jpg"), 320000));
        register(new Product("Hoa Cúc Mâm Xôi",    "Hoa tết",       "🌻", photo("cuc-mam-xoi.jpg"), 260000));
        register(new Product("Hoa Lan Hồ Điệp Tết","Hoa tết",       "🌷", photo("lan-ho-diep.jpg"), 450000));

        // ── Hoa Chia Buồn ──
        register(new Product("Hoa Cúc Trắng",       "Hoa Chia buồn", "🤍", photo("hoa-cuc-trang.jpg"), 190000));
        register(new Product("Hoa Huệ Trắng",       "Hoa Chia buồn", "⚪", photo("hoa-hue-trang.jpg"), 210000));
        register(new Product("Hoa Ly Trắng",        "Hoa Chia buồn", "🕊", photo("hoa-ly-trang.jpg"), 240000));
        register(new Product("Hoa Hồng Trắng",      "Hoa Chia buồn", "🥀", photo("hoa-hong-trang.jpg"), 220000));

        // ── Hoa Chúc Mừng ──
        register(new Product("Hoa Hướng Dương",      "Hoa Chúc Mừng", "🌻", photo("huong-duong.jpg"), 230000));
        register(new Product("Hoa Hồng Đỏ",         "Hoa Chúc Mừng", "🌹", photo("hoa-hong-do.jpg"), 250000));
        register(new Product("Hoa Tulip",            "Hoa Chúc Mừng", "🌷", photo("hoa-tulip.jpg"), 310000));
        register(new Product("Hoa Cẩm Chướng",      "Hoa Chúc Mừng", "🌸", photo("hoa-cam-chuong.jpg"), 205000));

        // ── Hoa Sinh Nhật ──
        register(new Product("Hoa Hồng Phấn",       "Hoa Sinh Nhật", "🌷", photo("hoa-hong-phan.jpg"), 275000));
        register(new Product("Hoa Đồng Tiền",       "Hoa Sinh Nhật", "🌼", photo("hoa-dong-tien.jpg"), 195000));
        register(new Product("Hoa Baby Trắng",      "Hoa Sinh Nhật", "🤍", photo("hoa-baby.jpg"), 180000));
        register(new Product("Hoa Lan Mokara",       "Hoa Sinh Nhật", "💜", photo("hoa-mokara.jpg"), 350000));

        // ── Hoa Cưới ──
        register(new Product("Hoa Hồng Pastel",     "Hoa Cưới",      "💐", photo("hong-pastel.jpg"), 380000));
        register(new Product("Hoa Cát Tường",       "Hoa Cưới",      "🌿", photo("hoa-cat-tuong.jpg"), 290000));
        register(new Product("Hoa Mẫu Đơn",        "Hoa Cưới",      "🌺", photo("hoa-mau-don.jpg"), 420000));
        register(new Product("Hoa Phi Yến",         "Hoa Cưới",      "💠", photo("hoa-phi-yen.jpg"), 340000));
    }

    private CartStore() {
    }

    private static void register(Product product) {
        CATALOG.put(product.getName(), product);
        CART.put(product.getName(), 0);
    }

    public static Map<String, Product> getCatalog() {
        return CATALOG;
    }

    public static Map<String, Integer> getCartSnapshot() {
        return new LinkedHashMap<>(CART);
    }

    public static Product getProduct(String name) {
        Product p = CATALOG.get(name);
        if (p != null) return p;
        for (Product product : CATALOG.values()) {
            if (product.getName().equalsIgnoreCase(name)) return product;
        }
        return null;
    }

    public static String getFlowerEmoji(String name) {
        Product p = CATALOG.get(name);
        if (p != null) return p.getEmoji();
        for (Product product : CATALOG.values()) {
            if (product.getName().equalsIgnoreCase(name)) return product.getEmoji();
        }
        return "🌸";
    }

    public static boolean addItem(String name) {
        if (!CART.containsKey(name)) {
            return false;
        }
        if (!InventoryStore.consumeStock(name, 1)) {
            return false;
        }
        CART.put(name, CART.get(name) + 1);
        return true;
    }

    /** Xoá một đơn vị sản phẩm khỏi giỏ hàng và trả lại tồn kho */
    public static boolean removeItem(String name) {
        Integer qty = CART.get(name);
        if (qty == null || qty <= 0) return false;
        CART.put(name, qty - 1);
        InventoryStore.releaseStock(name, 1);
        return true;
    }

    /** Xoá TẤT CẢ đơn vị sản phẩm khỏi giỏ hàng và trả lại tồn kho */
    public static boolean removeAllOfItem(String name) {
        Integer qty = CART.get(name);
        if (qty == null || qty <= 0) return false;
        CART.put(name, 0);
        InventoryStore.releaseStock(name, qty);
        return true;
    }

    /** Xoá giỏ hàng mà KHÔNG hoàn kho (sản phẩm đã được mua) */
    public static void clearCartAfterPurchase() {
        for (String key : CART.keySet()) {
            CART.put(key, 0);
        }
    }

    public static int getCartCount() {
        int total = 0;
        for (int qty : CART.values()) {
            total += qty;
        }
        return total;
    }

    public static double getSubtotal() {
        double subtotal = 0;
        for (Map.Entry<String, Integer> entry : CART.entrySet()) {
            Product product = CATALOG.get(entry.getKey());
            subtotal += product.getPrice() * entry.getValue();
        }
        return subtotal;
    }

    public static String buildCartPreview() {
        StringBuilder preview = new StringBuilder();
        int shown = 0;
        for (Map.Entry<String, Integer> entry : CART.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            if (shown > 0) {
                preview.append("\n");
            }
            preview.append(entry.getKey()).append(": ").append(entry.getValue());
            shown++;
            if (shown == 4) {
                break;
            }
        }

        if (shown == 0) {
            return "Giỏ hàng đang trống";
        }
        return preview.toString();
    }
}