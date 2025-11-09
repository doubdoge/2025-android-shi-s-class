package com.example.deltaforcedemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {
    private TransactionManager transactionManager;
    private TransactionAdapter transactionAdapter;
    private ListView transactionListView;
    private TextView emptyTextView;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        // 获取当前用户名
        HafCurrencyManager currencyManager = HafCurrencyManager.getInstance();
        try {
            currentUsername = currencyManager.getCurrentUsername();
        } catch (RuntimeException e) {
            finish();
            return;
        }

        // 初始化交易记录管理器
        transactionManager = TransactionManager.getInstance(this, currentUsername);

        // 初始化UI
        transactionListView = findViewById(R.id.lv_transactions);
        emptyTextView = findViewById(R.id.tv_empty);
        Button backButton = findViewById(R.id.btn_back);

        // 加载交易记录
        List<TransactionRecord> records = transactionManager.getTransactionRecords();
        transactionAdapter = new TransactionAdapter(this, records);
        transactionListView.setAdapter(transactionAdapter);

        // 显示/隐藏空列表提示
        if (records.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            transactionListView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            transactionListView.setVisibility(View.VISIBLE);
        }

        // 返回按钮
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新交易记录（以防在其他页面有新交易）
        if (transactionManager != null && transactionAdapter != null) {
            List<TransactionRecord> records = transactionManager.getTransactionRecords();
            transactionAdapter.updateData(records);
            
            // 更新空列表提示
            if (records.isEmpty()) {
                emptyTextView.setVisibility(View.VISIBLE);
                transactionListView.setVisibility(View.GONE);
            } else {
                emptyTextView.setVisibility(View.GONE);
                transactionListView.setVisibility(View.VISIBLE);
            }
        }
    }
}

