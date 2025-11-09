package com.example.deltaforcedemo;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionManager {
    private static TransactionManager instance;
    private final List<TransactionRecord> transactionRecords = new ArrayList<>();
    private final Context context;
    private final String username;
    private final Gson gson;
    private static final String PREF_PREFIX = "TransactionData_"; // 用户名作为前缀
    private static final String KEY_TRANSACTIONS = "transaction_records";

    private TransactionManager(Context context, String username) {
        this.context = context.getApplicationContext();
        this.username = username;
        this.gson = new Gson();
        loadData(); // 初始化时加载该用户的交易记录
    }

    public static synchronized TransactionManager getInstance(Context context, String username) {
        if (instance == null || !instance.username.equals(username)) {
            instance = new TransactionManager(context, username);
        }
        return instance;
    }

    /**
     * 添加交易记录
     */
    public void addTransaction(TransactionRecord record) {
        transactionRecords.add(record);
        saveData(); // 保存数据
    }

    /**
     * 获取所有交易记录（按时间倒序排列，最新的在前）
     */
    public List<TransactionRecord> getTransactionRecords() {
        List<TransactionRecord> sortedList = new ArrayList<>(transactionRecords);
        Collections.sort(sortedList, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
        return sortedList;
    }

    /**
     * 保存交易记录数据
     */
    public void saveData() {
        SharedPreferences sp = context.getSharedPreferences(PREF_PREFIX + username, Context.MODE_PRIVATE);
        String json = gson.toJson(transactionRecords);
        sp.edit().putString(KEY_TRANSACTIONS, json).apply();
    }

    /**
     * 加载交易记录数据
     */
    private void loadData() {
        SharedPreferences sp = context.getSharedPreferences(PREF_PREFIX + username, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_TRANSACTIONS, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<TransactionRecord>>() {}.getType();
            List<TransactionRecord> savedRecords = gson.fromJson(json, type);
            if (savedRecords != null) {
                transactionRecords.clear();
                transactionRecords.addAll(savedRecords);
            }
        }
    }
}

