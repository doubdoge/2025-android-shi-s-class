package com.example.deltaforcedemo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 哈夫币管理类（单例模式，全局管理货币）
 */
public class HafCurrencyManager {
    private static HafCurrencyManager instance;
    private BigDecimal balance; // 用BigDecimal避免浮点数精度问题

    // 私有构造器（单例）
    private HafCurrencyManager() {
        // 初始金额：100万哈夫币
        this.balance = new BigDecimal("1000000");
    }

    // 获取单例实例
    public static synchronized HafCurrencyManager getInstance() {
        if (instance == null) {
            instance = new HafCurrencyManager();
        }
        return instance;
    }

    // 获取当前余额
    public BigDecimal getBalance() {
        return balance;
    }

    // 增加余额（出售物品时调用）
    public void addBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(amount).setScale(2, RoundingMode.HALF_UP);
        }
    }

    // 扣除余额（购买物品时调用，返回是否成功）
    public boolean deductBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        // 检查余额是否足够
        if (balance.compareTo(amount) >= 0) {
            balance = balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
            return true;
        }
        return false;
    }

    /**
     * 格式化货币显示
     * - <100万：直接显示（保留2位小数）
     * - 100万~1亿：显示为 "Xk"（X=金额/1000）
     * - 1亿~100亿：显示为 "Xm"（X=金额/100万）
     * - ≥100亿：显示为 "Xb"（X=金额/10亿）
     */
    public String formatBalance() {
        BigDecimal balance = this.balance.setScale(2, RoundingMode.HALF_UP);
        BigDecimal million = new BigDecimal("1000000");    // 100万
        BigDecimal hundredMillion = new BigDecimal("100000000"); // 1亿
        BigDecimal tenBillion = new BigDecimal("10000000000"); // 100亿

        if (balance.compareTo(tenBillion) >= 0) {
            // ≥100亿：除以10亿，单位b
            BigDecimal value = balance.divide(new BigDecimal("1000000000"), 1, RoundingMode.HALF_UP);
            return value + "b";
        } else if (balance.compareTo(hundredMillion) >= 0) {
            // 1亿~100亿：除以100万，单位m
            BigDecimal value = balance.divide(million, 1, RoundingMode.HALF_UP);
            return value + "m";
        } else if (balance.compareTo(million) >= 0) {
            // 100万~1亿：除以1000，单位k
            BigDecimal value = balance.divide(new BigDecimal("1000"), 1, RoundingMode.HALF_UP);
            return value + "k";
        } else {
            // <100万：直接显示
            return balance.toPlainString();
        }
    }
}