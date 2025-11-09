package com.example.deltaforcedemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.SharedPreferences;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;

public class MarketActivity extends AppCompatActivity implements MarketAdapter.OnTradeListener, WarehouseAdapter.OnSellListener {
    private List<MarketItem> marketItems = new ArrayList<>(); // 所有物品（完整列表）
    private List<MarketItem> filteredMarketItems = new ArrayList<>(); // 过滤后的物品列表
    private MarketAdapter marketAdapter;
    private WarehouseAdapter warehouseAdapter;
    private WarehouseManager warehouseManager;
    private TransactionManager transactionManager;
    private Timer priceTimer;
    private MyHandler myHandler;
    private TextView hafBalanceTv;
    private HafCurrencyManager currencyManager;
    private String currentUsername;
    private EditText searchEditText;
    private static final String PREF_MARKET = "MarketData";
    private static final String KEY_LAST_RESET_DATE = "last_reset_date";

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
                // 检查是否需要重置（每天0点重置）
                activity.checkAndResetMarketIfNeeded();
                
                // 价格和库存波动
                for (MarketItem item : activity.marketItems) {
                    item.fluctuatePrice();
                    item.fluctuateSupply(); // 同时波动库存
                }
                // 更新过滤后的列表
                activity.filterMarketItems(activity.getSearchQuery());
                activity.marketAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 隐藏ActionBar，移除"delta force demo"标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
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

        // 初始化过滤列表（开始时显示所有物品）
        filteredMarketItems = new ArrayList<>(marketItems);

        ListView marketListView = findViewById(R.id.lv_market);
        ListView warehouseListView = findViewById(R.id.lv_warehouse);
        searchEditText = findViewById(R.id.et_search);
        marketAdapter = new MarketAdapter(this, filteredMarketItems, this);
        warehouseAdapter = new WarehouseAdapter(this, warehouseManager.getWarehouseItems(), marketItems, this);
        marketListView.setAdapter(marketAdapter);
        warehouseListView.setAdapter(warehouseAdapter);

        // 设置搜索功能
        setupSearchFunction();

        // 交易记录按钮
        findViewById(R.id.btn_transaction_history).setOnClickListener(v -> {
            Intent intent = new Intent(MarketActivity.this, TransactionHistoryActivity.class);
            startActivity(intent);
        });

        // 金手指按钮 - 无条件获得100万货币
        findViewById(R.id.btn_cheat).setOnClickListener(v -> {
            BigDecimal cheatAmount = new BigDecimal("1000000");
            currencyManager.addBalance(cheatAmount);
            updateHafBalanceDisplay();
            showToast("金手指激活！获得 1000000 哈夫币");
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
        marketItems.add(new MarketItem("12.7x55mm PS12B", 6043, 1000, "12.7x55mm "));
        marketItems.add(new MarketItem("12.7x55mm  PD12双头弹", 2967, 800, "12.7x55mm "));
        marketItems.add(new MarketItem("12.7x55mm  PS12", 2537, 2000, "12.7x55mm "));
        marketItems.add(new MarketItem("12.7x55mm  PS12A", 111, 1200, "12.7x55mm "));

        marketItems.add(new MarketItem("5.45x39mm BS", 4309, 500, "5.45x39mm"));
        marketItems.add(new MarketItem("5.45x39mm BT", 1441, 1000, "5.45x39mm"));
        marketItems.add(new MarketItem("5.45x39mm PS", 549, 800, "5.45x39mm"));
        marketItems.add(new MarketItem("5.45x39mm T", 100, 2000, "5.45x39mm"));
        marketItems.add(new MarketItem("5.45x39mm PRS", 40, 1200, "5.45x39mm"));

        marketItems.add(new MarketItem("5.56x45mm M995", 4277, 500, "5.56x45mm"));
        marketItems.add(new MarketItem("5.56x45mm M855A1", 1581, 1000, "5.56x45mm"));
        marketItems.add(new MarketItem("5.56x45mm M855", 526, 800, "5.56x45mm"));
        marketItems.add(new MarketItem("5.56x45mm FMJ", 79, 2000, "5.56x45mm"));
        marketItems.add(new MarketItem("5.56x45mm RRLP", 35, 1200, "5.56x45mm"));

        marketItems.add(new MarketItem("5.8x42mm DVC12", 4655, 500, "5.8x42mm"));
        marketItems.add(new MarketItem("5.8x42mm DBP10", 1778, 1000, "5.8x42mm"));
        marketItems.add(new MarketItem("5.8x42mm DVP88", 358, 800, "5.8x42mm"));
        marketItems.add(new MarketItem("5.8x42mm DBP87", 94, 2000, "5.8x42mm"));

        marketItems.add(new MarketItem("6.8x51mm Hybrid", 4789, 1200, "6.8x51mm"));
        marketItems.add(new MarketItem("6.8x51mm FMJ", 1709, 500, "6.8x51mm"));

        marketItems.add(new MarketItem("7.62x51mm M62", 111, 1200, "7.62x51mm"));
        marketItems.add(new MarketItem("7.62x51mm M80", 43, 500, "7.62x51mm"));
        marketItems.add(new MarketItem("7.62x51mm BPZ", 111, 1200, "7.62x51mm"));
        marketItems.add(new MarketItem("7.62x51mm Ultra Nosler", 119, 500, "7.62x51mm"));

        marketItems.add(new MarketItem("7.62x54mm BT", 4294, 1200, "7.62x54mm"));
        marketItems.add(new MarketItem("7.62x54mm LPS", 1730, 500, "7.62x54mm"));
        marketItems.add(new MarketItem("7.62x54mm T46M", 602, 1200, "7.62x54mm"));

        marketItems.add(new MarketItem("9x39mm BP", 4614, 1200, "9x39mm"));
        marketItems.add(new MarketItem("9x39mm SP6", 1801, 500, "9x39mm"));
        marketItems.add(new MarketItem("9x39mm SP5", 524, 1200, "9x39mm"));
        marketItems.add(new MarketItem("45-70Govt FTX", 6215, 1200, "45-70Govt"));
        marketItems.add(new MarketItem("45-70Govt  FMJ", 2123, 500, "45-70Govt"));
        marketItems.add(new MarketItem("45-70Govt  RN", 1004, 1200, "45-70Govt"));




    }

    private void startPriceTimer() {
        priceTimer = new Timer();
        priceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                myHandler.sendEmptyMessage(1);
            }
        }, 0, 8000);
        
        // 启动时检查一次是否需要重置
        checkAndResetMarketIfNeeded();
    }
    
    /**
     * 检查并重置市场数据（如果到了新的一天）
     * 每天0点时重置，如果应用在0点之后启动，也会在启动时检测并重置
     */
    private void checkAndResetMarketIfNeeded() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
        int currentYear = calendar.get(Calendar.YEAR);
        
        // 获取上次重置的日期
        SharedPreferences sp = getSharedPreferences(PREF_MARKET, MODE_PRIVATE);
        int lastResetDay = sp.getInt(KEY_LAST_RESET_DATE + "_day", -1);
        int lastResetYear = sp.getInt(KEY_LAST_RESET_DATE + "_year", -1);
        
        boolean shouldReset = false;
        
        if (lastResetDay == -1 || lastResetYear == -1) {
            // 第一次运行，记录当前日期，不重置
            sp.edit()
                    .putInt(KEY_LAST_RESET_DATE + "_day", currentDay)
                    .putInt(KEY_LAST_RESET_DATE + "_year", currentYear)
                    .apply();
        } else {
            // 检查是否到了新的一天（日期变化）
            boolean isNewDay = (currentYear > lastResetYear) || 
                              (currentYear == lastResetYear && currentDay > lastResetDay);
            
            // 如果日期变化了，说明已经过了0点，需要重置
            if (isNewDay) {
                shouldReset = true;
            }
        }
        
        if (shouldReset) {
            // 重置所有物品到初始值
            runOnUiThread(() -> {
                for (MarketItem item : marketItems) {
                    item.resetToInitial();
                }
                // 更新过滤列表
                filterMarketItems(getSearchQuery());
                showToast("市场数据已重置为初始值（每日0点重置）");
            });
            
            // 更新上次重置日期
            sp.edit()
                    .putInt(KEY_LAST_RESET_DATE + "_day", currentDay)
                    .putInt(KEY_LAST_RESET_DATE + "_year", currentYear)
                    .apply();
        }
    }

    @Override
    public void onBuy(MarketItem item) {
        showTradeDialog(item, true, 0);
    }

    @Override
    public void onSell(WarehouseItem warehouseItem, MarketItem marketItem) {
        showTradeDialog(marketItem, false, warehouseItem.getQuantity());
    }

    private void showTradeDialog(MarketItem item, boolean isBuy, int maxQuantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_trade, null);
        builder.setView(view);
        EditText quantityEt = view.findViewById(R.id.et_quantity);
        TextView unitPriceTv = view.findViewById(R.id.tv_unit_price);
        TextView totalPriceTv = view.findViewById(R.id.tv_total_price);
        TextView balanceInfoTv = view.findViewById(R.id.tv_balance_info);
        
        String title = isBuy ? "购买 " + item.getName() : "出售 " + item.getName();
        builder.setTitle(title);
        
        // 显示单价
        BigDecimal unitPrice = item.getCurrentPrice();
        unitPriceTv.setText("单价: " + unitPrice + " 元");
        
        // 如果是出售，显示最大可出售数量提示
        TextView titleTv = view.findViewById(R.id.tv_title);
        if (!isBuy && maxQuantity > 0) {
            if (titleTv != null) {
                titleTv.setText("最多可出售: " + maxQuantity);
                titleTv.setVisibility(View.VISIBLE);
            }
        } else {
            // 购买时隐藏提示
            if (titleTv != null) {
                titleTv.setVisibility(View.GONE);
            }
        }
        
        // 获取当前余额
        BigDecimal currentBalance = currencyManager.getBalance();
        
        // 添加文本监听器，实时计算价格
        quantityEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                if (input.isEmpty() || input.equals("0")) {
                    totalPriceTv.setText("总价: 0 元");
                    balanceInfoTv.setText("");
                    return;
                }
                
                try {
                    int quantity = Integer.parseInt(input);
                    if (quantity <= 0) {
                        totalPriceTv.setText("总价: 0 元");
                        balanceInfoTv.setText("");
                        return;
                    }
                    
                    // 如果是出售，检查数量不能超过库存
                    if (!isBuy && maxQuantity > 0 && quantity > maxQuantity) {
                        totalPriceTv.setText("总价: " + unitPrice.multiply(new BigDecimal(quantity)) + " 元");
                        balanceInfoTv.setText("⚠️ 出售数量超过库存！");
                        balanceInfoTv.setTextColor(ContextCompat.getColor(MarketActivity.this, android.R.color.holo_red_dark));
                        return;
                    }
                    
                    // 计算总价
                    BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(quantity));
                    totalPriceTv.setText("总价: " + totalPrice + " 元");
                    
                    // 根据购买或出售显示不同信息
                    if (isBuy) {
                        // 购买：显示余额对比
                        if (currentBalance.compareTo(totalPrice) >= 0) {
                            BigDecimal remaining = currentBalance.subtract(totalPrice);
                            balanceInfoTv.setText("✓ 余额充足 | 当前余额: " + currentBalance + " 元 | 剩余: " + remaining + " 元");
                            balanceInfoTv.setTextColor(ContextCompat.getColor(MarketActivity.this, android.R.color.holo_green_dark));
                        } else {
                            BigDecimal shortage = totalPrice.subtract(currentBalance);
                            balanceInfoTv.setText("✗ 余额不足 | 当前余额: " + currentBalance + " 元 | 缺少: " + shortage + " 元");
                            balanceInfoTv.setTextColor(ContextCompat.getColor(MarketActivity.this, android.R.color.holo_red_dark));
                        }
                    } else {
                        // 出售：显示将获得的金额
                        BigDecimal newBalance = currentBalance.add(totalPrice);
                        balanceInfoTv.setText("将获得: " + totalPrice + " 元 | 余额将变为: " + newBalance + " 元");
                        balanceInfoTv.setTextColor(ContextCompat.getColor(MarketActivity.this, android.R.color.holo_green_dark));
                    }
                } catch (NumberFormatException e) {
                    totalPriceTv.setText("总价: 0 元");
                    balanceInfoTv.setText("");
                }
            }
        });

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

            // 如果是出售，检查数量不能超过库存
            if (!isBuy && maxQuantity > 0 && quantity > maxQuantity) {
                showToast("出售数量不能超过库存数量: " + maxQuantity);
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

    /**
     * 设置搜索功能
     */
    private void setupSearchFunction() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                filterMarketItems(query);
            }
        });
    }

    /**
     * 获取当前搜索关键词
     */
    private String getSearchQuery() {
        if (searchEditText != null) {
            return searchEditText.getText().toString().trim();
        }
        return "";
    }

    /**
     * 过滤市场物品
     * @param query 搜索关键词
     */
    private void filterMarketItems(String query) {
        if (query.isEmpty()) {
            // 如果搜索框为空，显示所有物品
            filteredMarketItems.clear();
            filteredMarketItems.addAll(marketItems);
        } else {
            // 过滤物品：搜索名称和类型
            filteredMarketItems.clear();
            String lowerQuery = query.toLowerCase();
            
            for (MarketItem item : marketItems) {
                String itemName = item.getName().toLowerCase();
                String itemType = item.getType().toLowerCase();
                
                // 检查名称或类型是否包含搜索关键词
                if (itemName.contains(lowerQuery) || itemType.contains(lowerQuery)) {
                    filteredMarketItems.add(item);
                }
            }
        }
        
        // 更新适配器数据
        marketAdapter.updateData(filteredMarketItems);
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