package com.example.deltaforcedemo;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class UserAccountManager {
    private static UserAccountManager instance;
    private final SharedPreferences sp;
    private final Gson gson;
    private static final String PREF_NAME = "RegisteredAccounts";
    private static final String KEY_USER_LIST = "user_list"; // 改用List+JSON存储用户名
    private static final String KEY_PWD_PREFIX = "pwd_";

    public static synchronized UserAccountManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserAccountManager(context.getApplicationContext());
        }
        return instance;
    }

    private UserAccountManager(Context context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson(); // 用Gson序列化List
    }

    /**
     * 注册账号（修复存储逻辑）
     */
    public boolean registerAccount(String username, String password) {
        // 1. 读取已注册用户名列表（JSON转List）
        String userListJson = sp.getString(KEY_USER_LIST, "[]");
        List<String> userList = gson.fromJson(userListJson, new TypeToken<List<String>>() {}.getType());

        // 2. 校验用户名是否已存在
        if (userList.contains(username)) {
            return false;
        }

        // 3. 新增用户名并重新存储（关键：JSON序列化后存入）
        userList.add(username);
        sp.edit().putString(KEY_USER_LIST, gson.toJson(userList)).apply();

        // 4. 存储密码（键：pwd_用户名，确保与账户唯一关联）
        sp.edit().putString(KEY_PWD_PREFIX + username, password).apply();
        return true;
    }

    /**
     * 登录校验（修复读取逻辑）
     */
    public int checkLogin(String username, String password) {
        // 1. 读取已注册用户名列表
        String userListJson = sp.getString(KEY_USER_LIST, "[]");
        List<String> userList = gson.fromJson(userListJson, new TypeToken<List<String>>() {}.getType());

        // 2. 校验账号是否存在
        if (!userList.contains(username)) {
            return 1; // 账号不存在
        }

        // 3. 校验密码（读取对应用户名的密码）
        String savedPwd = sp.getString(KEY_PWD_PREFIX + username, "");
        return savedPwd.equals(password) ? 0 : 2; // 0成功，2密码错误
    }

    /**
     * 验证账号是否已注册
     */
    public boolean isAccountExists(String username) {
        String userListJson = sp.getString(KEY_USER_LIST, "[]");
        List<String> userList = gson.fromJson(userListJson, new TypeToken<List<String>>() {}.getType());
        return userList.contains(username);
    }
}