package com.group5.safezone.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Glide imports removed - no image loading needed
import com.group5.safezone.R;
import com.group5.safezone.model.entity.Order;
import com.group5.safezone.model.entity.Product;
// ProductImages import removed - no image loading needed
import com.group5.safezone.model.entity.Report;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.view.OrderDetailActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SalesOrderAdapter extends RecyclerView.Adapter<SalesOrderAdapter.SalesOrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private List<Product> products;
    private List<User> buyers;
    // productImages field removed - no image loading needed
    private List<Report> reports;

    public SalesOrderAdapter(Context context, List<Order> orders, List<Product> products, 
                           List<User> buyers, List<Report> reports) {
        this.context = context;
        this.orders = orders;
        this.products = products;
        this.buyers = buyers;
        this.reports = reports;
    }

    @NonNull
    @Override
    public SalesOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sales_order, parent, false);
        return new SalesOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesOrderViewHolder holder, int position) {
        Order order = orders.get(position);
        
        // Find related data
        Product product = findProductById(order.getProductId());
        User buyer = findUserById(order.getUserId());
        Report report = findReportByOrderId(order.getId());

        // Set order info
        holder.tvOrderId.setText("📦 Đơn hàng #" + order.getId());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvOrderDate.setText(dateFormat.format(order.getOrderDate()));

        // Set product info
        if (product != null) {
            holder.tvProductName.setText(product.getProductName());
            holder.tvOrderPrice.setText("💰 Giá: " + formatPrice(order.getPrice()));
        }

        // Set buyer info
        if (buyer != null) {
            holder.tvBuyerName.setText("👤 Người mua: " + buyer.getUserName());
        }

        // Set product image - Hide image to avoid errors
        if (holder.ivProductImage != null) {
            holder.ivProductImage.setVisibility(View.GONE);
        }

        // Set status
        holder.tvOrderStatus.setText(order.getStatus());
        setStatusBackground(holder.tvOrderStatus, order.getStatus());

        // Handle complaint if exists
        if (report != null && "Khiếu Nại".equals(report.getStatus())) {
            holder.layoutComplaintInfo.setVisibility(View.VISIBLE);
            holder.btnProcessComplaint.setVisibility(View.VISIBLE);
            holder.tvComplaintDescription.setText(report.getDescribe());
        } else {
            holder.layoutComplaintInfo.setVisibility(View.GONE);
            holder.btnProcessComplaint.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = OrderDetailActivity.newIntent(context, order.getId());
            context.startActivity(intent);
        });

        holder.btnProcessComplaint.setOnClickListener(v -> {
            Intent intent = OrderDetailActivity.newIntent(context, order.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private Product findProductById(String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    private User findUserById(int userId) {
        for (User user : buyers) {
            if (user.getId() == userId) {
                return user;
            }
        }
        return null;
    }

    // Product image finding removed - no image loading needed

    private Report findReportByOrderId(int orderId) {
        for (Report report : reports) {
            if (report.getOrderId() == orderId) {
                return report;
            }
        }
        return null;
    }

    private void setStatusBackground(TextView statusView, String status) {
        switch (status) {
            case "chờ người mua xác nhận nhận":
                statusView.setBackgroundResource(R.drawable.status_warning_background);
                break;
            case "Đã hoàn thành":
                statusView.setBackgroundResource(R.drawable.status_success_background);
                break;
            case "Đã hủy":
                statusView.setBackgroundResource(R.drawable.status_error_background);
                break;
            default:
                statusView.setBackgroundResource(R.drawable.status_success_background);
                break;
        }
    }

    private String formatPrice(double price) {
        return String.format("%,.0f VNĐ", price);
    }
    
    // Image loading removed to avoid errors - only show text information

    public static class SalesOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvProductName, tvBuyerName, tvOrderPrice, tvOrderStatus, tvComplaintDescription;
        ImageView ivProductImage;
        Button btnViewDetails, btnProcessComplaint;
        LinearLayout layoutComplaintInfo;

        public SalesOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBuyerName = itemView.findViewById(R.id.tvBuyerName);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvComplaintDescription = itemView.findViewById(R.id.tvComplaintDescription);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnProcessComplaint = itemView.findViewById(R.id.btnProcessComplaint);
            layoutComplaintInfo = itemView.findViewById(R.id.layoutComplaintInfo);
        }
    }
}
