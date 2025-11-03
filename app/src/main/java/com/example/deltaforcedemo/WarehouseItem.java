package com.example.deltaforcedemo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WarehouseItem {
    private String name; // 物品名称
    private int quantity; // 数量
    private BigDecimal averagePrice; // 平均价格

    // 必须保留空构造器（Gson反序列化需要）
    public WarehouseItem() {}

    // 构造方法（初始化物品）
    public WarehouseItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.averagePrice = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    // Getter和Setter（必须实现，Gson需要）
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getAveragePrice() { return averagePrice; }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }

    // 新增数量时更新平均价格
    public void addQuantity(int addCount, double newPrice) {
        BigDecimal totalCost = averagePrice.multiply(BigDecimal.valueOf(quantity))
                .add(BigDecimal.valueOf(newPrice).multiply(BigDecimal.valueOf(addCount)));
        quantity += addCount;
        averagePrice = totalCost.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
    }
}