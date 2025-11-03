package com.example.deltaforcedemo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WarehouseItem {
    private String name;
    private int quantity;
    private BigDecimal averagePrice; // 平均买入价

    public WarehouseItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.averagePrice = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    // 更新平均价格（新增数量时）
    public void addQuantity(int addCount, double newPrice) {
        BigDecimal totalCost = averagePrice.multiply(BigDecimal.valueOf(quantity))
                .add(BigDecimal.valueOf(newPrice).multiply(BigDecimal.valueOf(addCount)));
        quantity += addCount;
        averagePrice = totalCost.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
    }

    // Getter和Setter
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public BigDecimal getAveragePrice() { return averagePrice; }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity); // 确保数量不为负
    }
}
