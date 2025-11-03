package com.example.deltaforcedemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class WarehouseAdapter extends BaseAdapter {
    private final Context context;
    private List<WarehouseItem> warehouseItems;

    public WarehouseAdapter(Context context, List<WarehouseItem> items) {
        this.context = context;
        this.warehouseItems = items;
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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        WarehouseItem item = warehouseItems.get(position);
        holder.nameTv.setText(item.getName());
        holder.quantityTv.setText("持有数量: " + item.getQuantity());
        holder.priceTv.setText("平均成本: " + item.getAveragePrice() + " 元");

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv, quantityTv, priceTv;
    }
}
