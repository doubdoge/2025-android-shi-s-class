package com.example.deltaforcedemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MarketAdapter extends BaseAdapter {
    private final Context context;
    private final List<MarketItem> marketItems;
    private final OnTradeListener tradeListener;

    public interface OnTradeListener {
        void onBuy(MarketItem item);
    }

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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MarketItem item = marketItems.get(position);
        holder.nameTv.setText(item.getName() + " (" + item.getType() + ")");
        holder.priceTv.setText("价格: " + item.getCurrentPrice() + " 元");
        holder.stockTv.setText("库存: " + item.getTotalSupply());

        // 1. 修正方法名：getchangeRate() → getChangeRate()（大小写错误）
        // 2. 处理类型：double → BigDecimal（转换后保留2位小数）
        double changeRate = item.getChangeRate(); // 获取double类型的变化率
        BigDecimal rateBigDecimal = BigDecimal.valueOf(changeRate)
                .multiply(BigDecimal.valueOf(100)) // 转为百分比（如0.02 → 2%）
                .setScale(2, RoundingMode.HALF_UP); // 保留2位小数

        // 格式化涨跌幅文本
        String changeText = rateBigDecimal.doubleValue() >= 0 ?
                "+" + rateBigDecimal + "%" : rateBigDecimal + "%";
        holder.changeTv.setText(changeText);

        // 涨跌幅颜色（红涨绿跌）
        if (rateBigDecimal.compareTo(BigDecimal.ZERO) > 0) {
            holder.changeTv.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else if (rateBigDecimal.compareTo(BigDecimal.ZERO) < 0) {
            holder.changeTv.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.changeTv.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // 按钮点击事件
        holder.buyBtn.setOnClickListener(v -> {
            if (tradeListener != null) {
                tradeListener.onBuy(item);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv, priceTv, changeTv, stockTv;
        Button buyBtn;
    }
}