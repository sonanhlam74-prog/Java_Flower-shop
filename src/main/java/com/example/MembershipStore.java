package com.example;

/**
 * Hệ thống hạng thành viên dựa trên tổng chi tiêu.
 *
 * Hạng:
 *   🥉 Đồng   (Bronze)    — mặc định, giảm  0%
 *   🥈 Bạc    (Silver)    — từ  500.000₫, giảm  5%
 *   🥇 Vàng   (Gold)      — từ 1.500.000₫, giảm 10%
 *   💎 Kim Cương (Diamond) — từ 3.000.000₫, giảm 15%
 *   👑 VIP                 — từ 5.000.000₫, giảm 20%
 */
public final class MembershipStore {

    public enum Tier {
        BRONZE("Đồng",      0,         0.00, "#cd7f32", "https://img.icons8.com/emoji/48/3rd-place-medal-emoji.png"),
        SILVER("Bạc",       500_000,   0.05, "#94a3b8", "https://img.icons8.com/emoji/48/2nd-place-medal-emoji.png"),
        GOLD("Vàng",        1_500_000, 0.10, "#f59e0b", "https://img.icons8.com/emoji/48/1st-place-medal-emoji.png"),
        DIAMOND("Kim Cương", 3_000_000, 0.15, "#38bdf8", "https://img.icons8.com/emoji/48/gem-stone.png"),
        VIP("VIP",           5_000_000, 0.20, "#a855f7", "https://img.icons8.com/nolan/64/vip.png");

        private final String displayName;
        private final double threshold;
        private final double discountRate;
        private final String color;
        private final String iconUrl;

        Tier(String displayName, double threshold, double discountRate, String color, String iconUrl) {
            this.displayName = displayName;
            this.threshold = threshold;
            this.discountRate = discountRate;
            this.color = color;
            this.iconUrl = iconUrl;
        }

        public String getDisplayName() { return displayName; }
        public double getThreshold()   { return threshold; }
        public double getDiscountRate() { return discountRate; }
        public String getColor()       { return color; }
        public String getIconUrl()     { return iconUrl; }
    }

    private static double totalSpent = 0;

    private MembershipStore() {}

    /** Ghi nhận chi tiêu sau khi thanh toán thành công */
    public static void recordPurchase(double amount) {
        totalSpent += amount;
    }

    /** Tổng chi tiêu tích lũy */
    public static double getTotalSpent() {
        return totalSpent;
    }

    /** Hạng hiện tại dựa theo tích lũy */
    public static Tier getCurrentTier() {
        Tier current = Tier.BRONZE;
        for (Tier t : Tier.values()) {
            if (totalSpent >= t.getThreshold()) {
                current = t;
            }
        }
        return current;
    }

    /** % giảm giá hiện tại */
    public static double getDiscountRate() {
        return getCurrentTier().getDiscountRate();
    }

    /** Số tiền cần chi thêm để lên hạng tiếp theo (0 nếu đã VIP) */
    public static double getAmountToNextTier() {
        Tier current = getCurrentTier();
        Tier[] tiers = Tier.values();
        int idx = current.ordinal();
        if (idx >= tiers.length - 1) return 0;
        return tiers[idx + 1].getThreshold() - totalSpent;
    }

    /** Hạng kế tiếp (null nếu đã VIP) */
    public static Tier getNextTier() {
        Tier current = getCurrentTier();
        Tier[] tiers = Tier.values();
        int idx = current.ordinal();
        if (idx >= tiers.length - 1) return null;
        return tiers[idx + 1];
    }


    /** Tóm tắt trạng thái thành viên */
    public static String getSummary() {
        Tier tier = getCurrentTier();
        StringBuilder sb = new StringBuilder();
        sb.append(tier.getDisplayName());
        if (tier.getDiscountRate() > 0) {
            sb.append(" • Giảm ").append((int)(tier.getDiscountRate() * 100)).append("%");
        }
        Tier next = getNextTier();
        if (next != null) {
            sb.append(" • Còn ").append(String.format("%,.0f", getAmountToNextTier())).append("₫ → ").append(next.getDisplayName());
        } else {
            sb.append(" • Hạng cao nhất!");
        }
        return sb.toString();
    }

}
