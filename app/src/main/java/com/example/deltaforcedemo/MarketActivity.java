package com.example.deltaforcedemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MarketActivity extends AppCompatActivity implements MarketAdapter.OnTradeListener {
    private List<MarketItem> marketItems = new ArrayList<>();
    private MarketAdapter marketAdapter;
    private WarehouseAdapter warehouseAdapter;
    private WarehouseManager warehouseManager;
    private TransactionManager transactionManager;
    private Timer priceTimer;
    private MyHandler myHandler;
    private TextView hafBalanceTv;
    private HafCurrencyManager currencyManager;
    private String currentUsername;

    private static class MyHandler extends Handler {
        private final WeakReference<MarketActivity> activityRef;
        public MyHandler(MarketActivity activity) {
            super(Looper.getMainLooper());
            activityRef = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MarketActivity activity = activityRef.get();
            if (activity == null) return;
            if (msg.what == 1) {
                for (MarketItem item : activity.marketItems) {
                    item.fluctuatePrice();
                }
                activity.marketAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        // 1. 先校验登录状态（未登录直接跳转）
        currencyManager = HafCurrencyManager.getInstance();
        try {
            currentUsername = currencyManager.getCurrentUsername();
        } catch (RuntimeException e) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2. 初始化仓库（绑定当前用户）
        warehouseManager = WarehouseManager.getInstance(this, currentUsername);
        
        // 3. 初始化交易记录管理器（绑定当前用户）
        transactionManager = TransactionManager.getInstance(this, currentUsername);

        // 4. 初始化UI（加载已保存的货币余额）
        hafBalanceTv = findViewById(R.id.tv_haf_balance);
        updateHafBalanceDisplay();

        myHandler = new MyHandler(this);
        initMarketData();

        ListView marketListView = findViewById(R.id.lv_market);
        ListView warehouseListView = findViewById(R.id.lv_warehouse);
        marketAdapter = new MarketAdapter(this, marketItems, this);
        warehouseAdapter = new WarehouseAdapter(this, warehouseManager.getWarehouseItems());
        marketListView.setAdapter(marketAdapter);
        warehouseListView.setAdapter(warehouseAdapter);

        // 交易记录按钮
        findViewById(R.id.btn_transaction_history).setOnClickListener(v -> {
            Intent intent = new Intent(MarketActivity.this, TransactionHistoryActivity.class);
            startActivity(intent);
        });

        startPriceTimer();
    }

    private void updateHafBalanceDisplay() {
        hafBalanceTv.setText(currencyManager.formatBalance());
    }

    private void initMarketData() {
        marketItems.add(new MarketItem("7.62x39mm AP", 4071, 1000, "7.62x39mm"));
        marketItems.add(new MarketItem("7.62x39mm BP", 1261, 800, "7.62x39mm"));
        marketItems.add(new MarketItem("7.62x39mm PS", 506, 2000, "7.62x39mm"));
        marketItems.add(new MarketItem("7.62x39mm T45M", 111, 1200, "7.62x39mm"));
        marketItems.add(new MarketItem("7.62x39mm LP", 43, 500, "7.62x39mm"));
    }

    private void startPriceTimer() {
        priceTimer = new Timer();
        priceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                myHandler.sendEmptyMessage(1);
            }
        }, 0, 8000);
    }

    @Override
    public void onBuy(MarketItem item) {
        showTradeDialog(item, true);
    }

    @Override
    public void onSell(MarketItem item) {
        showTradeDialog(item, false);
    }

    private void showTradeDialog(MarketItem item, boolean isBuy) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_trade, null);
        builder.setView(view);
        EditText quantityEt = view.findViewById(R.id.et_quantity);
        String title = isBuy ? "购买 " + item.getName() : "出售 " + item.getName();
        builder.setTitle(title);

        builder.setPositiveButton("确认", (dialog, which) -> {
            String input = quantityEt.getText().toString().trim();
            if (input.isEmpty()) {
                showToast("请输入数量");
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(input);
                if (quantity <= 0) {
                    showToast("数量必须大于0");
                    return;
                }
            } catch (NumberFormatException e) {
                showToast("请输入有效数字");
                return;
            }

            BigDecimal amount = item.getCurrentPrice().multiply(new BigDecimal(quantity));
            boolean success;

            if (isBuy) {
                if (currencyManager.getBalance().compareTo(amount) < 0) {
                    showToast("哈夫币不足");
                    return;
                }
                success = currencyManager.deductBalance(amount) && warehouseManager.buyItem(item, quantity);
            } else {
                success = warehouseManager.sellItem(item, quantity);
                if (success) {
                    currencyManager.addBalance(amount);
                }
            }

            if (success) {
                // 记录交易
                TransactionRecord record = new TransactionRecord(
                    item.getName(),
                    isBuy,
                    quantity,
                    amount,
                    item.getCurrentPrice()
                );
                transactionManager.addTransaction(record);
                
                marketAdapter.notifyDataSetChanged();
                warehouseAdapter.updateData(warehouseManager.getWarehouseItems());
                updateHafBalanceDisplay();
                showToast(isBuy ? "购买成功" : "出售成功");
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关键：退出页面时强制保存货币和仓库数据（双重保障）
        currencyManager.saveAccountBalance();
        warehouseManager.saveData(); // 调用仓库的保存方法

        if (priceTimer != null) {
            priceTimer.cancel();
            priceTimer.purge();
        }
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
        }
    }
}