package com.example.chain_g;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<ProductItem> items;

    public ProductAdapter(List<ProductItem> items) {
        this.items = items;
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
        holder.tvMakeDate.setText(item.productionDate);
        holder.tvExpDate.setText(item.expiryDate);
        
        holder.btnDelete.setOnClickListener(v -> {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUid, tvPCode, tvPName, tvMakeDate, tvExpDate, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUid = itemView.findViewById(R.id.tv_uid);
            tvPCode = itemView.findViewById(R.id.tv_p_code);
            tvPName = itemView.findViewById(R.id.tv_p_name);
            tvMakeDate = itemView.findViewById(R.id.tv_make_date);
            tvExpDate = itemView.findViewById(R.id.tv_exp_date);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public static class ProductItem {
        String identifier, productCode, productName, productionDate, expiryDate;

        public ProductItem(String identifier, String productCode, String productName, String productionDate, String expiryDate) {
            this.identifier = identifier;
            this.productCode = productCode;
            this.productName = productName;
            this.productionDate = productionDate;
            this.expiryDate = expiryDate;
        }
    }
}