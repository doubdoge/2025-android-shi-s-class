package com.example.deltaforcedemo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends BaseAdapter {
    private final Context context;
    private List<TransactionRecord> transactionRecords;
    private final SimpleDateFormat dateFormat;

    public TransactionAdapter(Context context, List<TransactionRecord> records) {
        this.context = context;
        this.transactionRecords = records;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    // 更新数据
    public void updateData(List<TransactionRecord> newRecords) {
        this.transactionRecords = newRecords;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return transactionRecords.size();
    }

    @Override
    public Object getItem(int position) {
        return transactionRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
            holder = new ViewHolder();
            holder.itemNameTv = convertView.findViewById(R.id.tv_item_name);
            holder.transactionTypeTv = convertView.findViewById(R.id.tv_transaction_type);
            holder.quantityTv = convertView.findViewById(R.id.tv_quantity);
            holder.unitPriceTv = convertView.findViewById(R.id.tv_unit_price);
            holder.totalAmountTv = convertView.findViewById(R.id.tv_total_amount);
            holder.timeTv = convertView.findViewById(R.id.tv_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TransactionRecord record = transactionRecords.get(position);
        holder.itemNameTv.setText(record.getItemName());
        
        // 设置交易类型（购买/出售）
        if (record.isBuy()) {
            holder.transactionTypeTv.setText("购买");
            holder.transactionTypeTv.setTextColor(Color.parseColor("#4CAF50")); // 绿色
        } else {
            holder.transactionTypeTv.setText("出售");
            holder.transactionTypeTv.setTextColor(Color.parseColor("#F44336")); // 红色
        }
        
        holder.quantityTv.setText("数量: " + record.getQuantity());
        holder.unitPriceTv.setText("单价: " + record.getUnitPrice() + " 哈夫币");
        holder.totalAmountTv.setText("总金额: " + record.getTotalAmount() + " 哈夫币");
        holder.timeTv.setText("时间: " + dateFormat.format(new Date(record.getTimestamp())));

        return convertView;
    }

    static class ViewHolder {
        TextView itemNameTv, transactionTypeTv, quantityTv, unitPriceTv, totalAmountTv, timeTv;
    }
}

