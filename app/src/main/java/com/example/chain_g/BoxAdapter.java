package com.example.chain_g;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BoxAdapter extends RecyclerView.Adapter<BoxAdapter.ViewHolder> {

    private List<BoxItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String boxCode);
    }

    public BoxAdapter(List<BoxItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BoxItem item = items.get(position);
        holder.tvBoxCode.setText(item.boxCode);
        holder.tvProductCode.setText(item.productCode);
        holder.tvItemName.setText(item.itemName);
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item.boxCode));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBoxCode, tvProductCode, tvItemName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBoxCode = itemView.findViewById(R.id.tv_box_code);
            tvProductCode = itemView.findViewById(R.id.tv_product_code);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
        }
    }

    public static class BoxItem {
        String boxCode;
        String productCode;
        String itemName;

        public BoxItem(String boxCode, String productCode, String itemName) {
            this.boxCode = boxCode;
            this.productCode = productCode;
            this.itemName = itemName;
        }
    }
}