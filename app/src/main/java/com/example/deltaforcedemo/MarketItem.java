package com.example.deltaforcedemo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MarketItem {
    private String name;
    private BigDecimal currentPrice;
    private int totalSupply;
    private int initialSupply; // 初始库存值
    private String type;
    private final Random random = new Random();
    private double changeRate; // 新增：价格变化率（百分比，如0.02表示+2%，-0.03表示-3%）

    public MarketItem(String name, int initialPrice, int totalSupply, String type) {
        this.name = name;
        this.currentPrice = BigDecimal.valueOf(initialPrice).setScale(2, RoundingMode.HALF_UP);
        this.totalSupply = totalSupply;
        this.initialSupply = totalSupply; // 保存初始库存值
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

    /**
     * 库存波动逻辑：随机上下浮动（±20%），但保持在初始值的80%-120%范围内
     */
    public void fluctuateSupply() {
        // 生成-20%到+20%之间的随机波动比例（[-0.20, 0.20)）
        double fluctuation = (random.nextDouble() * 0.4) - 0.20;
        
        // 基于初始库存值计算新库存
        double newSupply = initialSupply * (1 + fluctuation);
        
        // 确保新库存在初始值的80%-120%范围内
        int minSupply = (int) (initialSupply * 0.8);
        int maxSupply = (int) (initialSupply * 1.2);
        
        // 四舍五入并限制在范围内
        int finalSupply = (int) Math.round(newSupply);
        if (finalSupply < minSupply) {
            finalSupply = minSupply;
        } else if (finalSupply > maxSupply) {
            finalSupply = maxSupply;
        }
        
        // 确保库存不为负数
        if (finalSupply < 0) {
            finalSupply = 0;
        }
        
        this.totalSupply = finalSupply;
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
        // 如果设置的新库存值超出了初始值的80%-120%范围，需要调整
        // 但这里不自动调整，因为可能是用户交易导致的正常变化
    }

    /**
     * 获取初始库存值
     */
    public int getInitialSupply() {
        return initialSupply;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}