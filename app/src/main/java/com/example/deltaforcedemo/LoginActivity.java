package com.example.deltaforcedemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegister; // 新增：注册按钮
    private UserAccountManager accountManager; // 账号管理工具

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化账号管理工具
        accountManager = UserAccountManager.getInstance(this);

        // 初始化控件
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register); // 绑定注册按钮

        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> attemptLogin());

        // 新增：注册按钮点击事件（弹出注册对话框）
        btnRegister.setOnClickListener(v -> showRegisterDialog());
    }

    /**
     * 登录逻辑（添加账号密码校验）
     */
    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. 输入校验
        if (TextUtils.isEmpty(username)) {
            showToast("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showToast("请输入密码");
            return;
        }

        // 2. 登录校验（调用账号管理工具）
        int loginResult = accountManager.checkLogin(username, password);
        switch (loginResult) {
            case 0: // 登录成功
                // 初始化账户（余额、仓库数据）
                HafCurrencyManager currencyManager = HafCurrencyManager.getInstance();
                currencyManager.initAccount(username, password);
                // 跳转到市场页面
                Intent intent = new Intent(LoginActivity.this, MarketActivity.class);
                startActivity(intent);
                finish();
                break;
            case 1: // 账号不存在
                showToast("该账号未注册，请先注册");
                break;
            case 2: // 密码错误
                showToast("密码错误，请重新输入");
                break;
        }
    }

    /**
     * 显示注册对话框（输入用户名、密码、确认密码）
     */
    private void showRegisterDialog() {
        // 1. 加载注册对话框布局（需创建 layout/dialog_register.xml）
        View registerView = getLayoutInflater().inflate(R.layout.dialog_register, null);
        EditText etRegUsername = registerView.findViewById(R.id.et_reg_username);
        EditText etRegPwd = registerView.findViewById(R.id.et_reg_pwd);
        EditText etRegPwdConfirm = registerView.findViewById(R.id.et_reg_pwd_confirm);

        // 2. 构建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("账号注册")
                .setView(registerView)
                .setPositiveButton("注册", (dialog, which) -> {
                    // 3. 获取注册输入信息
                    String regUsername = etRegUsername.getText().toString().trim();
                    String regPwd = etRegPwd.getText().toString().trim();
                    String regPwdConfirm = etRegPwdConfirm.getText().toString().trim();

                    // 4. 注册输入校验
                    if (TextUtils.isEmpty(regUsername)) {
                        showToast("请输入用户名");
                        return;
                    }
                    if (TextUtils.isEmpty(regPwd)) {
                        showToast("请输入密码");
                        return;
                    }
                    if (TextUtils.isEmpty(regPwdConfirm)) {
                        showToast("请确认密码");
                        return;
                    }
                    if (!regPwd.equals(regPwdConfirm)) {
                        showToast("两次密码输入不一致");
                        return;
                    }
                    if (regPwd.length() < 6) { // 可选：密码长度限制
                        showToast("密码长度不能少于6位");
                        return;
                    }

                    // 5. 调用注册方法
                    boolean registerSuccess = accountManager.registerAccount(regUsername, regPwd);
                    if (registerSuccess) {
                        showToast("注册成功！请登录");
                        dialog.dismiss();
                    } else {
                        showToast("用户名已存在，请更换");
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}