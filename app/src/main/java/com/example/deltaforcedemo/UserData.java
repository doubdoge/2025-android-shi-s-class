package com.example.deltaforcedemo;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户业务数据模型（需保存的数据）
 */
public class UserData {
    @SerializedName("funds") // 资金余额
    private double funds;

    @SerializedName("warehouseItems") // 仓库物品（名称→数量）
    private Map<String, Integer> warehouseItems;

    // 构造方法（初始化默认数据）
    public UserData() {
        this.funds = 10000.0; // 初始资金
        this.warehouseItems = new HashMap<>(); // 初始空仓库
    }

    // Getter和Setter
    public double getFunds() { return funds; }
    public void setFunds(double funds) { this.funds = funds; }

    public Map<String, Integer> getWarehouseItems() { return warehouseItems; }
    public void setWarehouseItems(Map<String, Integer> items) { this.warehouseItems = items; }
}