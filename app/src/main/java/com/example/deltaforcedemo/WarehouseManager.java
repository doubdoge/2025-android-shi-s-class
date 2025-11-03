package com.example.deltaforcedemo;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 仓库管理器（仅管理物品，货币逻辑已移至HafCurrencyManager）
 */
public class WarehouseManager {
    private static WarehouseManager instance;
    private final List<WarehouseItem> warehouseItems = new ArrayList<>();

    private WarehouseManager() {}

    public static synchronized WarehouseManager getInstance() {
        if (instance == null) {
            instance = new WarehouseManager();
        }
        return instance;
    }

    public List<WarehouseItem> getWarehouseItems() {
        return new ArrayList<>(warehouseItems);
    }

    // 购买物品（仅管理库存，货币扣减在MarketActivity中处理）
    public boolean buyItem(Context context, MarketItem marketItem, int quantity) {
        // 检查市场库存是否足够
        if (marketItem.getTotalAmount() < quantity) {
            Toast.makeText(context, "市场库存不足", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 减少市场库存
        marketItem.setTotalAmount(marketItem.getTotalAmount() - quantity);

        // 增加仓库库存
        for (WarehouseItem item : warehouseItems) {
            if (item.getName().equals(marketItem.getName())) {
                item.setQuantity(item.getQuantity() + quantity);
                return true;
            }
        }

        // 仓库中没有该物品，新增（修复参数不匹配问题）
        warehouseItems.add(new WarehouseItem(
                marketItem.getName(),
                quantity,
                marketItem.getPrice().doubleValue()
        ));
        return true;
    }

    // 出售物品（仅管理库存，货币增加在MarketActivity中处理）
    public boolean sellItem(Context context, MarketItem marketItem, int quantity) {
        // 检查仓库库存是否足够
        for (WarehouseItem item : warehouseItems) {
            if (item.getName().equals(marketItem.getName())) {
                if (item.getQuantity() < quantity) {
                    Toast.makeText(context, "仓库库存不足", Toast.LENGTH_SHORT).show();
                    return false;
                }

                // 减少仓库库存
                item.setQuantity(item.getQuantity() - quantity);
                if (item.getQuantity() == 0) {
                    warehouseItems.remove(item); // 库存为0时移除
                }

                // 增加市场库存
                marketItem.setTotalAmount(marketItem.getTotalAmount() + quantity);
                return true;
            }
        }

        Toast.makeText(context, "仓库中没有该物品", Toast.LENGTH_SHORT).show();
        return false;
    }
}