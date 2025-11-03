package com.example.deltaforcedemo;

import android.content.Context;
import android.content.SharedPreferences;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HafCurrencyManager {
    private static HafCurrencyManager instance;
    private BigDecimal balance;
    private String currentAccountKey; // 账户唯一标识（用户名+密码哈希）
    private String currentUsername;
    private static final String PREF_CURRENCY = "HafCurrencyData"; // 货币存储文件名

    private HafCurrencyManager() {}

    public static synchronized HafCurrencyManager getInstance() {
        if (instance == null) {
            instance = new HafCurrencyManager();
        }
        return instance;
    }

    /**
     * 初始化账户（登录成功后调用，绑定账户与货币）
     */
    public void initAccount(String username, String password) {
        this.currentUsername = username;
        this.currentAccountKey = generateAccountKey(username, password);
        this.balance = loadAccountBalance(); // 加载该账户的货币
    }

    /**
     * 生成唯一账户标识（避免存储键冲突）
     */
    private String generateAccountKey(String username, String password) {
        try {
            String input = username + "_" + password;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return username + "_" + password; // 降级方案
        }
    }

    /**
     * 加载账户货币（新账户默认1000000）
     */
    private BigDecimal loadAccountBalance() {
        if (currentAccountKey == null) {
            throw new RuntimeException("请先登录");
        }
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(PREF_CURRENCY, Context.MODE_PRIVATE);
        String savedBalance = sp.getString(currentAccountKey, null);
        // 新账户默认1000000，老账户加载存储值
        return savedBalance == null ? new BigDecimal("1000000") : new BigDecimal(savedBalance);
    }

    /**
     * 强制保存货币（确保每次变动都持久化）
     */
    public void saveAccountBalance() {
        if (currentAccountKey == null) return;
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(PREF_CURRENCY, Context.MODE_PRIVATE);
        sp.edit().putString(currentAccountKey, balance.toPlainString()).apply();
    }

    // ------------------- 原有方法强化（添加保存逻辑）-------------------
    public BigDecimal getBalance() {
        return balance;
    }

    public void addBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(amount).setScale(2, RoundingMode.HALF_UP);
            saveAccountBalance(); // 实时保存
        }
    }

    public boolean deductBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (balance.compareTo(amount) >= 0) {
            balance = balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
            saveAccountBalance(); // 实时保存
            return true;
        }
        return false;
    }

    public String formatBalance() {
        BigDecimal balance = this.balance.setScale(2, RoundingMode.HALF_UP);
        BigDecimal million = new BigDecimal("1000000");
        BigDecimal hundredMillion = new BigDecimal("100000000");
        BigDecimal tenBillion = new BigDecimal("10000000000");

        if (balance.compareTo(tenBillion) >= 0) {
            return balance.divide(new BigDecimal("1000000000"), 1, RoundingMode.HALF_UP) + "b";
        } else if (balance.compareTo(hundredMillion) >= 0) {
            return balance.divide(million, 1, RoundingMode.HALF_UP) + "m";
        } else if (balance.compareTo(million) >= 0) {
            return balance.divide(new BigDecimal("1000"), 1, RoundingMode.HALF_UP) + "k";
        } else {
            return balance.toPlainString();
        }
    }

    public String getCurrentUsername() {
        if (currentUsername == null) throw new RuntimeException("请先登录");
        return currentUsername;
    }
}