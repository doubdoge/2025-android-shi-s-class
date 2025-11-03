package com.example.deltaforcedemo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MarketItem {
    private String name;
    private BigDecimal currentPrice;
    private int totalSupply;
    private String type;
    private final Random random = new Random();
    private double changeRate; // 新增：价格变化率（百分比，如0.02表示+2%，-0.03表示-3%）

    public MarketItem(String name, int initialPrice, int totalSupply, String type) {
        this.name = name;
        this.currentPrice = BigDecimal.valueOf(initialPrice).setScale(2, RoundingMode.HALF_UP);
        this.totalSupply = totalSupply;
        this.type = type;
        this.changeRate = 0.0; // 初始变化率为0
    }

    /**
     * 价格波动逻辑：随机上下浮动（±5%），并记录变化率
     */
    public void fluctuatePrice() {
        // 生成-5%到+5%之间的随机波动比例（[-0.05, 0.05)）
        double fluctuation = (random.nextDouble() * 0.1) - 0.05;
        // 记录变化率（保留4位小数，便于后续显示百分比）
        this.changeRate = Math.round(fluctuation * 10000) / 10000.0;

        // 计算新价格
        BigDecimal newPrice = currentPrice.multiply(BigDecimal.valueOf(1 + fluctuation))
                .setScale(2, RoundingMode.HALF_UP);
        if (newPrice.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            newPrice = BigDecimal.valueOf(0.01);
        }
        currentPrice = newPrice;
    }

    // 新增：获取价格变化率（返回值为小数，如0.02表示+2%）
    public double getChangeRate() {
        return changeRate;
    }

    // 原有Getter和Setter方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public int getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(int totalSupply) {
        this.totalSupply = totalSupply;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}