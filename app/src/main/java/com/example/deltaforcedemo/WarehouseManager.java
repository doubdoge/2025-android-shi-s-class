package com.example.deltaforcedemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WarehouseManager {
    private static WarehouseManager instance;
    private final List<WarehouseItem> warehouseItems = new ArrayList<>();
    private final Context context;
    private final String username;
    private final Gson gson;
    private static final String PREF_PREFIX = "WarehouseData_"; // 用户名作为前缀
    private static final String KEY_ITEMS = "warehouse_items";

    private WarehouseManager(Context context, String username) {
        this.context = context.getApplicationContext();
        this.username = username;
        this.gson = new Gson();
        loadData(); // 初始化时加载该用户的仓库数据
    }

    public static synchronized WarehouseManager getInstance(Context context, String username) {
        if (instance == null || !instance.username.equals(username)) {
            instance = new WarehouseManager(context, username);
        }
        return instance;
    }

    public List<WarehouseItem> getWarehouseItems() {
        return new ArrayList<>(warehouseItems);
    }

    public boolean buyItem(MarketItem marketItem, int quantity) {
        if (marketItem.getTotalSupply() < quantity) {
            Toast.makeText(context, "市场库存不足", Toast.LENGTH_SHORT).show();
            return false;
        }
        marketItem.setTotalSupply(marketItem.getTotalSupply() - quantity);

        for (WarehouseItem item : warehouseItems) {
            if (item.getName().equals(marketItem.getName())) {
                item.addQuantity(quantity, marketItem.getCurrentPrice().doubleValue());
                saveData(); // 购买后保存
                return true;
            }
        }

        warehouseItems.add(new WarehouseItem(
                marketItem.getName(),
                quantity,
                marketItem.getCurrentPrice().doubleValue()
        ));
        saveData(); // 新增后保存
        return true;
    }

    public boolean sellItem(MarketItem marketItem, int quantity) {
        for (WarehouseItem item : warehouseItems) {
            if (item.getName().equals(marketItem.getName())) {
                if (item.getQuantity() < quantity) {
                    Toast.makeText(context, "仓库库存不足", Toast.LENGTH_SHORT).show();
                    return false;
                }
                item.setQuantity(item.getQuantity() - quantity);
                if (item.getQuantity() == 0) {
                    warehouseItems.remove(item);
                }
                marketItem.setTotalSupply(marketItem.getTotalSupply() + quantity);
                saveData(); // 出售后保存
                return true;
            }
        }
        Toast.makeText(context, "仓库中没有该物品", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 保存仓库数据（公开方法，供MarketActivity退出时调用）
     */
    public void saveData() {
        SharedPreferences sp = context.getSharedPreferences(PREF_PREFIX + username, Context.MODE_PRIVATE);
        String json = gson.toJson(warehouseItems);
        sp.edit().putString(KEY_ITEMS, json).apply();
    }

    /**
     * 加载仓库数据
     */
    private void loadData() {
        SharedPreferences sp = context.getSharedPreferences(PREF_PREFIX + username, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_ITEMS, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<WarehouseItem>>() {}.getType();
            List<WarehouseItem> savedItems = gson.fromJson(json, type);
            if (savedItems != null) {
                warehouseItems.clear();
                warehouseItems.addAll(savedItems);
            }
        }
    }
}