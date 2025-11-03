package com.example.deltaforcedemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MarketActivity extends AppCompatActivity implements MarketAdapter.OnTradeListener {
    private List<MarketItem> marketItems = new ArrayList<>();
    private MarketAdapter marketAdapter;
    private WarehouseAdapter warehouseAdapter;
    private WarehouseManager warehouseManager;
    private Timer priceTimer;
    private MyHandler myHandler;

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

        // 强制横屏
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        myHandler = new MyHandler(this);
        warehouseManager = WarehouseManager.getInstance();

        initMarketData();

        // 初始化列表
        ListView marketListView = findViewById(R.id.lv_market);
        ListView warehouseListView = findViewById(R.id.lv_warehouse);

        marketAdapter = new MarketAdapter(this, marketItems, this);
        warehouseAdapter = new WarehouseAdapter(this, warehouseManager.getWarehouseItems());

        marketListView.setAdapter(marketAdapter);
        warehouseListView.setAdapter(warehouseAdapter);

        startPriceTimer();
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

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String input = quantityEt.getText().toString().trim();
                if (input.isEmpty()) {
                    showToast("请输入数量");
                    return;
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    showToast("请输入有效数字");
                    return;
                }

                boolean success;
                if (isBuy) {
                    success = warehouseManager.buyItem(MarketActivity.this, item, quantity);
                } else {
                    success = warehouseManager.sellItem(MarketActivity.this, item, quantity);
                }

                if (success) {
                    marketAdapter.notifyDataSetChanged();
                    warehouseAdapter.updateData(warehouseManager.getWarehouseItems());
                }
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
        if (priceTimer != null) {
            priceTimer.cancel();
            priceTimer.purge();
        }
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
        }
    }
}