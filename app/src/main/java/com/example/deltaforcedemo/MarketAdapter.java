package com.example.deltaforcedemo;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

public class MarketAdapter extends BaseAdapter {
    private final Context context;
    private final List<MarketItem> marketItems;
    private final OnTradeListener tradeListener;

    // 交易监听器接口（与MarketActivity保持一致）
    public interface OnTradeListener {
        void onBuy(MarketItem item);
        void onSell(MarketItem item);
    }

    // 构造方法（与MarketActivity中初始化参数匹配）
    public MarketAdapter(Context context, List<MarketItem> items, OnTradeListener listener) {
        this.context = context;
        this.marketItems = items;
        this.tradeListener = listener;
    }

    @Override
    public int getCount() {
        return marketItems.size();
    }

    @Override
    public Object getItem(int position) {
        return marketItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_market, parent, false);
            holder = new ViewHolder();
            holder.nameTv = convertView.findViewById(R.id.tv_name);
            holder.priceTv = convertView.findViewById(R.id.tv_price);
            holder.changeTv = convertView.findViewById(R.id.tv_change);
            holder.stockTv = convertView.findViewById(R.id.tv_stock);
            holder.buyBtn = convertView.findViewById(R.id.btn_buy);
            holder.sellBtn = convertView.findViewById(R.id.btn_sell);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MarketItem item = marketItems.get(position);
        holder.nameTv.setText(item.getName() + " (" + item.getType() + ")");
        holder.priceTv.setText("价格: " + item.getPrice() + " 元");

        // 关键修复：getStock() → getTotalAmount()（与MarketItem类方法名一致）
        holder.stockTv.setText("库存: " + item.getTotalAmount());

        // 涨跌幅处理（已修复BigDecimal导入和setText歧义）
        BigDecimal changeRate = item.getChangeRate();
        String changeText = changeRate.doubleValue() >= 0 ?
                "+" + changeRate.toString() + "%" : changeRate.toString() + "%";
        holder.changeTv.setText(changeText);

        // 涨跌幅颜色
        if (changeRate.compareTo(BigDecimal.ZERO) > 0) {
            holder.changeTv.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else if (changeRate.compareTo(BigDecimal.ZERO) < 0) {
            holder.changeTv.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.changeTv.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // 按钮点击事件（与接口回调匹配）
        holder.buyBtn.setOnClickListener(v -> {
            if (tradeListener != null) {
                tradeListener.onBuy(item);
            }
        });

        holder.sellBtn.setOnClickListener(v -> {
            if (tradeListener != null) {
                tradeListener.onSell(item);
            }
        });

        return convertView;
    }

    // ViewHolder内部类
    static class ViewHolder {
        TextView nameTv, priceTv, changeTv, stockTv;
        Button buyBtn, sellBtn;
    }
}