package com.example.deltaforcedemo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MarketItem {
    private String name;
    private BigDecimal price; // 使用BigDecimal避免浮点数精度问题
    private BigDecimal changeRate; // 价格变动率(百分比)
    private int totalAmount; // 市场总数量
    private String type;

    public MarketItem(String name, double price, int totalAmount, String type) {
        this.name = name;
        this.price = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
        this.totalAmount = totalAmount;
        this.type = type;
        this.changeRate = BigDecimal.ZERO;
    }

    // 模拟价格波动（确保价格不为负）
    public void fluctuatePrice() {
        // 波动范围：-3% 到 +3%
        double fluctuation = (Math.random() * 6 - 3) / 100;
        BigDecimal newPrice = price.multiply(BigDecimal.valueOf(1 + fluctuation))
                .setScale(2, RoundingMode.HALF_UP);

        // 确保价格不会低于0.01
        if (newPrice.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            newPrice = BigDecimal.valueOf(0.01);
        }

        // 计算变动率
        changeRate = newPrice.subtract(price)
                .divide(price, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        price = newPrice;
    }

    // Getter和Setter
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getChangeRate() { return changeRate; }
    public int getTotalAmount() { return totalAmount; }
    public String getType() { return type; }

    public void setTotalAmount(int amount) {
        this.totalAmount = Math.max(0, amount); // 确保数量不为负
    }
}