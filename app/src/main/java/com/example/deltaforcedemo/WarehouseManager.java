package com.example.deltaforcedemo;


import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WarehouseManager {
    private static volatile WarehouseManager instance; // 双重检查锁定
    private final List<WarehouseItem> warehouseItems;

    private WarehouseManager() {
        warehouseItems = new ArrayList<>();
    }

    // 单例获取（线程安全）
    public static WarehouseManager getInstance() {
        if (instance == null) {
            synchronized (WarehouseManager.class) {
                if (instance == null) {
                    instance = new WarehouseManager();
                }
            }
        }
        return instance;
    }

    // 购买物品（返回是否成功）
    public boolean buyItem(Context context, MarketItem marketItem, int quantity) {
        if (quantity <= 0) {
            showToast(context, "数量必须大于0");
            return false;
        }

        if (marketItem.getTotalAmount() < quantity) {
            showToast(context, "市场库存不足");
            return false;
        }

        // 更新市场库存
        marketItem.setTotalAmount(marketItem.getTotalAmount() - quantity);

        // 检查仓库中是否已有该物品
        for (WarehouseItem item : warehouseItems) {
            if (item.getName().equals(marketItem.getName())) {
                item.addQuantity(quantity, marketItem.getPrice().doubleValue());
                showToast(context, "购买成功");
                return true;
            }
        }

        // 新增物品到仓库
        warehouseItems.add(new WarehouseItem(
                marketItem.getName(),
                quantity,
                marketItem.getPrice().doubleValue()
        ));
        showToast(context, "购买成功");
        return true;
    }

    // 出售物品（返回是否成功）
    public boolean sellItem(Context context, MarketItem marketItem, int quantity) {
        if (quantity <= 0) {
            showToast(context, "数量必须大于0");
            return false;
        }

        for (int i = 0; i < warehouseItems.size(); i++) {
            WarehouseItem item = warehouseItems.get(i);
            if (item.getName().equals(marketItem.getName())) {
                if (item.getQuantity() < quantity) {
                    showToast(context, "仓库库存不足");
                    return false;
                }

                // 更新仓库库存
                item.setQuantity(item.getQuantity() - quantity);
                // 更新市场库存
                marketItem.setTotalAmount(marketItem.getTotalAmount() + quantity);

                // 移除数量为0的物品
                if (item.getQuantity() == 0) {
                    warehouseItems.remove(i);
                }

                showToast(context, "出售成功");
                return true;
            }
        }

        showToast(context, "仓库中没有该物品");
        return false;
    }

    private void showToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public List<WarehouseItem> getWarehouseItems() {
        return new ArrayList<>(warehouseItems); // 返回副本，避免外部直接修改
    }
}