package com.example.deltaforcedemo;


import android.content.Context;
import android.content.SharedPreferences;
import com.example.deltaforcedemo.UserData;
import com.google.gson.Gson;

/**
 * 用户数据管理工具（持久化存储与读取）
 */
public class UserDataManager {
    private static final String SP_NAME = "UserData"; // SharedPreferences文件名
    private static final String KEY_PREFIX = "user_"; // 数据键前缀（用于区分不同用户）
    private static final Gson gson = new Gson();

    // 保存用户数据（与用户名绑定）
    public static void saveUserData(Context context, String username, UserData userData) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        // 数据键格式："user_用户名_data"（确保不同用户数据不冲突）
        String key = KEY_PREFIX + username + "_data";
        // 将UserData对象转为JSON字符串存储
        sp.edit().putString(key, gson.toJson(userData)).apply();
    }

    // 读取用户数据（根据用户名）
    public static UserData loadUserData(Context context, String username) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String key = KEY_PREFIX + username + "_data";
        String json = sp.getString(key, null);

        if (json != null) {
            // JSON字符串转UserData对象
            return gson.fromJson(json, UserData.class);
        } else {
            // 首次登录：返回默认数据
            return new UserData();
        }
    }

    // 清空指定用户的数据（可选：如注销功能）
    public static void clearUserData(Context context, String username) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String key = KEY_PREFIX + username + "_data";
        sp.edit().remove(key).apply();
    }
}
