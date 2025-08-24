package com.group5.safezone.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group5.safezone.R;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.model.dao.OrderDao;
import com.group5.safezone.model.dao.ReportDao;
import com.group5.safezone.model.entity.Order;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.model.entity.Report;
import com.group5.safezone.view.OrderDetailActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    
    private List<Order> orders;
    private List<Product> products;
    private List<String> sellerUsernames;
    private Context context;
    private ExecutorService executor;
    private OrderDao orderDao;
    
    public OrderAdapter() {
        this.orders = new ArrayList<>();
        this.products = new ArrayList<>();
        this.sellerUsernames = new ArrayList<>();
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void updateData(List<Order> orders, List<Product> products, List<String> sellerUsernames) {
        this.orders = orders != null ? orders : new ArrayList<>();
        this.products = products != null ? products : new ArrayList<>();
        this.sellerUsernames = sellerUsernames != null ? sellerUsernames : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        orderDao = AppDatabase.getDatabase(context).orderDao();
        
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        if (position < orders.size() && position < products.size() && position < sellerUsernames.size()) {
            Order order = orders.get(position);
            Product product = products.get(position);
            String sellerUsername = sellerUsernames.get(position);
            
            holder.bind(order, product, sellerUsername);
        }
    }
    
    @Override
    public int getItemCount() {
        return Math.min(orders.size(), Math.min(products.size(), sellerUsernames.size()));
    }
    
    class OrderViewHolder extends RecyclerView.ViewHolder {
        
        TextView tvOrderId, tvOrderStatus, tvProductName, tvSellerName;
        TextView tvOrderDate, tvOrderNote, tvOrderPrice;
        ImageView ivProductImage;
        Button btnViewProduct;
        
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvSellerName = itemView.findViewById(R.id.tvSellerName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderNote = itemView.findViewById(R.id.tvOrderNote);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnViewProduct = itemView.findViewById(R.id.btnViewProduct);
        }
        
        public void bind(Order order, Product product, String sellerUsername) {
            // Order ID
            tvOrderId.setText("Đơn hàng #" + order.getId());
            
            // Order Status
            tvOrderStatus.setText(order.getStatus());
            updateStatusBackground(order.getStatus());
            
            // Product Info
            tvProductName.setText(product.getProductName());
            tvSellerName.setText("Người bán: " + sellerUsername);
            // Quantity is always 1, no need to display
            
            // Order Details
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvOrderDate.setText(sdf.format(order.getOrderDate()));
            
            String note = order.getNote();
            if (note != null && !note.trim().isEmpty()) {
                tvOrderNote.setText(note);
            } else {
                tvOrderNote.setText("Không có");
            }
            
            // Price
            tvOrderPrice.setText(formatPrice(order.getPrice()));
            
            // Check complaint status
            checkComplaintStatus(order.getId());
            
            // Load product image
            loadProductImage(product.getId());
            
            // Setup buttons
            setupButtons(order, product);
        }
        
        private void updateStatusBackground(String status) {
            if ("chờ người mua xác nhận nhận".equals(status)) {
                tvOrderStatus.setBackgroundResource(R.drawable.status_pending_background);
            } else if ("đã hoàn thành".equals(status)) {
                tvOrderStatus.setBackgroundResource(R.drawable.status_completed_background);
            } else {
                tvOrderStatus.setBackgroundResource(R.drawable.status_pending_background);
            }
        }
        
        private void loadProductImage(String productId) {
            executor.execute(() -> {
                try {
                    List<ProductImages> images = AppDatabase.getDatabase(context).productImagesDao().getImagesByProductId(productId);
                    if (images != null && !images.isEmpty()) {
                        ProductImages firstImage = images.get(0);
                        String imagePath = firstImage.getPath();
                        
                        // Load image with Glide
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            if (imagePath != null && !imagePath.isEmpty()) {
                                Glide.with(context)
                                    .load(imagePath)
                                    .placeholder(R.drawable.ic_shopping_bag)
                                    .error(R.drawable.ic_shopping_bag)
                                    .into(ivProductImage);
                            }
                        });
                    }
                } catch (Exception e) {
                    // Use default image on error
                }
            });
        }
        
        private void setupButtons(Order order, Product product) {
            // View Order Detail button
            btnViewProduct.setOnClickListener(v -> {
                Intent intent = OrderDetailActivity.newIntent(context, order.getId());
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).startActivityForResult(intent, 1001);
                } else {
                    context.startActivity(intent);
                }
            });
        }
        

        
        private String formatPrice(double price) {
            return String.format("%,.0f VNĐ", price);
        }
        
        private void checkComplaintStatus(long orderId) {
            executor.execute(() -> {
                try {
                    // Get complaint status from database
                    List<Report> reports = AppDatabase.getDatabase(context).reportDao().getReportsByOrderId((int) orderId);
                    if (reports != null && !reports.isEmpty()) {
                        Report report = reports.get(0);
                        String status = report.getStatus();
                        
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            updateComplaintUI(status, report.getDescribe());
                        });
                    }
                } catch (Exception e) {
                    // Ignore errors
                }
            });
        }
        
        private void updateComplaintUI(String complaintStatus, String complaintText) {
            if ("Khiếu nại".equals(complaintStatus)) {
                tvOrderNote.setText("⚠️ Khiếu nại: " + complaintText);
                tvOrderNote.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                tvOrderNote.setBackgroundResource(android.R.color.holo_orange_light);
            } else if ("Cần admin xử lý".equals(complaintStatus)) {
                tvOrderNote.setText("⏳ Khiếu nại chờ admin xử lý: " + complaintText);
                tvOrderNote.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                tvOrderNote.setBackgroundResource(android.R.color.holo_red_light);
            } else if ("Đã chấp nhận".equals(complaintStatus)) {
                tvOrderNote.setText("✅ Khiếu nại đã được chấp nhận: " + complaintText);
                tvOrderNote.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                tvOrderNote.setBackgroundResource(android.R.color.holo_green_light);
            }
            
            // Make complaint text more visible
            tvOrderNote.setPadding(16, 8, 16, 8);
            tvOrderNote.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }
}
 