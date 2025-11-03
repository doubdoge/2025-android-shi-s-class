package com.example.deltaforcedemo;


import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化全局上下文
        context = getApplicationContext();
    }

    // 提供静态方法供其他类获取上下文
    public static Context getContext() {
        return context;
    }
}