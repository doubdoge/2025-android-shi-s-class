package com.example.deltaforcedemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class WarehouseAdapter extends BaseAdapter {
    private final Context context;
    private List<WarehouseItem> warehouseItems;
    private List<MarketItem> marketItems; // 用于获取当前市场价格
    private OnSellListener sellListener;

    public interface OnSellListener {
        void onSell(WarehouseItem warehouseItem, MarketItem marketItem);
    }

    public WarehouseAdapter(Context context, List<WarehouseItem> items, List<MarketItem> marketItems, OnSellListener listener) {
        this.context = context;
        this.warehouseItems = items;
        this.marketItems = marketItems;
        this.sellListener = listener;
    }

    // 更新数据
    public void updateData(List<WarehouseItem> newItems) {
        this.warehouseItems = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return warehouseItems.size();
    }

    @Override
    public Object getItem(int position) {
        return warehouseItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_warehouse, parent, false);
            holder = new ViewHolder();
            holder.nameTv = convertView.findViewById(R.id.tv_name);
            holder.quantityTv = convertView.findViewById(R.id.tv_quantity);
            holder.priceTv = convertView.findViewById(R.id.tv_avg_price);
            holder.sellBtn = convertView.findViewById(R.id.btn_sell);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        WarehouseItem item = warehouseItems.get(position);
        holder.nameTv.setText(item.getName());
        holder.quantityTv.setText("持有数量: " + item.getQuantity());
        holder.priceTv.setText("平均成本: " + item.getAveragePrice() + " 元");

        // 根据库存数量显示/隐藏卖出按钮
        if (item.getQuantity() > 0) {
            holder.sellBtn.setVisibility(View.VISIBLE);
            // 查找对应的市场商品以获取当前价格
            MarketItem marketItem = findMarketItem(item.getName());
            if (marketItem != null) {
                holder.sellBtn.setOnClickListener(v -> {
                    if (sellListener != null) {
                        sellListener.onSell(item, marketItem);
                    }
                });
            } else {
                holder.sellBtn.setVisibility(View.GONE);
            }
        } else {
            holder.sellBtn.setVisibility(View.GONE);
        }

        return convertView;
    }

    // 根据名称查找市场商品
    private MarketItem findMarketItem(String name) {
        if (marketItems == null) {
            return null;
        }
        for (MarketItem marketItem : marketItems) {
            if (marketItem.getName().equals(name)) {
                return marketItem;
            }
        }
        return null;
    }

    static class ViewHolder {
        TextView nameTv, quantityTv, priceTv;
        Button sellBtn;
    }
}
