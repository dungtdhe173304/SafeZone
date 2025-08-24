package com.group5.safezone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.model.entity.Order;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.Report;
import com.group5.safezone.model.entity.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminReportAdapter extends RecyclerView.Adapter<AdminReportAdapter.AdminReportViewHolder> {
    
    private Context context;
    private List<Report> reports;
    private List<Order> orders;
    private List<Product> products;
    private List<User> buyers;
    private List<User> sellers;
    
    private OnReportActionListener actionListener;
    
    public interface OnReportActionListener {
        void onSellerWin(Report report);
        void onBuyerWin(Report report);
    }
    
    public AdminReportAdapter(Context context, List<Report> reports, List<Order> orders, 
                            List<Product> products, List<User> buyers, List<User> sellers) {
        this.context = context;
        this.reports = reports;
        this.orders = orders;
        this.products = products;
        this.buyers = buyers;
        this.sellers = sellers;
    }
    
    public void setOnReportActionListener(OnReportActionListener listener) {
        this.actionListener = listener;
    }
    
    public void updateData(List<Report> reports, List<Order> orders, List<Product> products, 
                          List<User> buyers, List<User> sellers) {
        this.reports = reports;
        this.orders = orders;
        this.products = products;
        this.buyers = buyers;
        this.sellers = sellers;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public AdminReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_report, parent, false);
        return new AdminReportViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AdminReportViewHolder holder, int position) {
        if (position < reports.size()) {
            Report report = reports.get(position);
            Order order = position < orders.size() ? orders.get(position) : null;
            Product product = position < products.size() ? products.get(position) : null;
            User buyer = position < buyers.size() ? buyers.get(position) : null;
            User seller = position < sellers.size() ? sellers.get(position) : null;
            
            holder.bind(report, order, product, buyer, seller);
        }
    }
    
    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }
    
    class AdminReportViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvReportId, tvOrderId, tvProductName, tvBuyerName, tvSellerName;
        private TextView tvOrderPrice, tvReportDate, tvReportDescription, tvReportStatus;
        private Button btnSellerWin, btnBuyerWin;
        private LinearLayout llReportInfo;
        
        public AdminReportViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvReportId = itemView.findViewById(R.id.tvReportId);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBuyerName = itemView.findViewById(R.id.tvBuyerName);
            tvSellerName = itemView.findViewById(R.id.tvSellerName);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvReportDate = itemView.findViewById(R.id.tvReportDate);
            tvReportDescription = itemView.findViewById(R.id.tvReportDescription);
            tvReportStatus = itemView.findViewById(R.id.tvReportStatus);
            btnSellerWin = itemView.findViewById(R.id.btnSellerWin);
            btnBuyerWin = itemView.findViewById(R.id.btnBuyerWin);
            llReportInfo = itemView.findViewById(R.id.llReportInfo);
        }
        
        public void bind(Report report, Order order, Product product, User buyer, User seller) {
            if (report == null) return;
            
            // Report ID
            tvReportId.setText("Báo cáo #" + report.getId());
            
            // Order ID
            if (order != null) {
                tvOrderId.setText("Đơn hàng #" + order.getId());
                tvOrderPrice.setText(formatPrice(order.getPrice()));
            } else {
                tvOrderId.setText("Không tìm thấy đơn hàng");
                tvOrderPrice.setText("N/A");
            }
            
            // Product name
            if (product != null) {
                tvProductName.setText(product.getProductName());
            } else {
                tvProductName.setText("Không tìm thấy sản phẩm");
            }
            
            // Buyer name
            if (buyer != null) {
                tvBuyerName.setText("Người mua: " + buyer.getUserName());
            } else {
                tvBuyerName.setText("Người mua: Không tìm thấy");
            }
            
            // Seller name
            if (seller != null) {
                tvSellerName.setText("Người bán: " + seller.getUserName());
            } else {
                tvSellerName.setText("Người bán: Không tìm thấy");
            }
            
            // Report date
            if (report.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvReportDate.setText("Ngày báo cáo: " + sdf.format(report.getCreatedAt()));
            } else {
                tvReportDate.setText("Ngày báo cáo: N/A");
            }
            
            // Report description
            if (report.getDescribe() != null && !report.getDescribe().trim().isEmpty()) {
                tvReportDescription.setText("Nội dung: " + report.getDescribe());
            } else {
                tvReportDescription.setText("Nội dung: Không có mô tả");
            }
            
            // Report status
            tvReportStatus.setText("Trạng thái: " + report.getStatus());
            
            // Setup action buttons
            setupActionButtons(report);
        }
        
        private void setupActionButtons(Report report) {
            // Only show action buttons if status is "Cần admin xử lý"
            if ("Cần admin xử lý".equals(report.getStatus())) {
                btnSellerWin.setVisibility(View.VISIBLE);
                btnBuyerWin.setVisibility(View.VISIBLE);
                
                btnSellerWin.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onSellerWin(report);
                    }
                });
                
                btnBuyerWin.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onBuyerWin(report);
                    }
                });
            } else {
                // Hide buttons if already processed
                btnSellerWin.setVisibility(View.GONE);
                btnBuyerWin.setVisibility(View.GONE);
            }
        }
        
        private String formatPrice(double price) {
            return String.format("%,.0f VNĐ", price);
        }
    }
}
