package com.example.deltaforcedemo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TransactionRecord {
    private String itemName; // 物品名称
    private boolean isBuy; // true=购买, false=出售
    private int quantity; // 交易数量
    private BigDecimal totalAmount; // 总金额（哈夫币）
    private BigDecimal unitPrice; // 单价
    private long timestamp; // 交易时间戳

    // 必须保留空构造器（Gson反序列化需要）
    public TransactionRecord() {}

    public TransactionRecord(String itemName, boolean isBuy, int quantity, BigDecimal totalAmount, BigDecimal unitPrice) {
        this.itemName = itemName;
        this.isBuy = isBuy;
        this.quantity = quantity;
        this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.timestamp = System.currentTimeMillis();
    }

    // Getter和Setter（必须实现，Gson需要）
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public void setBuy(boolean buy) {
        isBuy = buy;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

