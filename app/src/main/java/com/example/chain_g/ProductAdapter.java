package com.example.chain_g;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<ProductItem> items;
    private OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDelete(int position);
    }

    public ProductAdapter(List<ProductItem> items) {
        this.items = items;
    }

    public ProductAdapter(List<ProductItem> items, OnItemDeleteListener deleteListener) {
        this.items = items;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductItem item = items.get(position);
        holder.tvUid.setText(item.identifier);
        holder.tvPCode.setText(item.productCode);
        holder.tvPName.setText(item.productName);

        // Conditional display based on whether it's a sale (unit price) or storage (dates)
        if (item.unitPrice != null) {
            holder.tvUnitPrice.setVisibility(View.VISIBLE);
            holder.tvMakeDate.setVisibility(View.GONE);
            holder.tvExpDate.setVisibility(View.GONE);
            
            DecimalFormat formatter = new DecimalFormat("#,###");
            holder.tvUnitPrice.setText(formatter.format(item.unitPrice));
        } else {
            holder.tvUnitPrice.setVisibility(View.GONE);
            holder.tvMakeDate.setVisibility(View.VISIBLE);
            holder.tvExpDate.setVisibility(View.VISIBLE);
            
            holder.tvMakeDate.setText(item.productionDate);
            holder.tvExpDate.setText(item.expiryDate);
        }

        // Background color based on scan status
        if (item.isScanned) {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#E7F5FF"));
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(position);
            } else {
                items.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, items.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUid, tvPCode, tvPName, tvMakeDate, tvExpDate, tvUnitPrice, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUid = itemView.findViewById(R.id.tv_uid);
            tvPCode = itemView.findViewById(R.id.tv_p_code);
            tvPName = itemView.findViewById(R.id.tv_p_name);
            tvMakeDate = itemView.findViewById(R.id.tv_make_date);
            tvExpDate = itemView.findViewById(R.id.tv_exp_date);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public static class ProductItem {
        public String identifier, productCode, productName, productionDate, expiryDate;
        public java.math.BigDecimal unitPrice;
        public boolean isScanned = false;

        public ProductItem(String identifier, String productCode, String productName, String productionDate, String expiryDate) {
            this.identifier = identifier;
            this.productCode = productCode;
            this.productName = productName;
            this.productionDate = productionDate;
            this.expiryDate = expiryDate;
        }

        public ProductItem(String identifier, String productCode, String productName, java.math.BigDecimal unitPrice) {
            this.identifier = identifier;
            this.productCode = productCode;
            this.productName = productName;
            this.unitPrice = unitPrice;
        }
    }
}