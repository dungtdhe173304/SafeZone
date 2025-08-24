package com.group5.safezone.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.model.entity.User;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserAuctionManagementActivity extends AppCompatActivity {
    
    private SessionManager sessionManager;
    private RecyclerView recyclerView;
    private UserAuctionAdapter adapter;
    private List<UserAuctionItem> userAuctions = new ArrayList<>();
    private ExecutorService executorService;
    private NumberFormat currencyFormatter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_auction_management);
        
        // Check user login
        sessionManager = new SessionManager(this);
        if (sessionManager.getUserId() <= 0) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        executorService = Executors.newFixedThreadPool(4);
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        initViews();
        loadUserAuctions();
    }
    
    private void initViews() {
        // Setup toolbar
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUserAuctions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new UserAuctionAdapter();
        recyclerView.setAdapter(adapter);
    }
    
    private void loadUserAuctions() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                int userId = sessionManager.getUserId();
                
                // Load auctions by seller user ID
                List<Auctions> auctions = db.auctionsDao().getAuctionsBySellerUserId(userId);
                
                userAuctions.clear();
                for (Auctions auction : auctions) {
                    try {
                        Product product = db.productDao().getProductById(auction.getProductId());
                        List<ProductImages> images = db.productImagesDao().getImagesByProductId(auction.getProductId());
                        User seller = db.userDao().getUserById(auction.getSellerUserId());
                        
                        if (product != null && seller != null) {
                            userAuctions.add(new UserAuctionItem(auction, product, seller, images));
                        }
                    } catch (Exception e) {
                        Log.e("UserAuction", "Error processing auction: " + e.getMessage(), e);
                    }
                }
                
                runOnUiThread(() -> {
                    adapter.submitList(new ArrayList<>(userAuctions));
                });
                
            } catch (Exception e) {
                Log.e("UserAuction", "Error loading user auctions: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải danh sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void editAuction(UserAuctionItem item) {
        // TODO: Implement edit functionality
        Toast.makeText(this, "Tính năng sửa bài đăng đang được phát triển", Toast.LENGTH_SHORT).show();
    }
    
    private void deleteAuction(UserAuctionItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa bài đấu giá này? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                executorService.execute(() -> {
                    try {
                        AppDatabase db = AppDatabase.getDatabase(this);
                        
                        // Delete images first
                        if (item.getImages() != null) {
                            for (ProductImages image : item.getImages()) {
                                if (image.getPath() != null) {
                                    File imageFile = new File(image.getPath());
                                    if (imageFile.exists()) {
                                        imageFile.delete();
                                    }
                                }
                                db.productImagesDao().delete(image);
                            }
                        }
                        
                        // Delete auction and product
                        db.auctionsDao().delete(item.getAuction());
                        db.productDao().delete(item.getProduct());
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã xóa bài đấu giá", Toast.LENGTH_LONG).show();
                            loadUserAuctions(); // Reload list
                        });
                        
                    } catch (Exception e) {
                        Log.e("UserAuction", "Error deleting auction: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    // Adapter for RecyclerView
    private class UserAuctionAdapter extends RecyclerView.Adapter<UserAuctionAdapter.ViewHolder> {
        
        private List<UserAuctionItem> items = new ArrayList<>();
        
        public void submitList(List<UserAuctionItem> newItems) {
            items.clear();
            if (newItems != null) {
                items.addAll(newItems);
            }
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_auction, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivProductImage;
            private TextView tvProductName, tvDescription, tvPrice, tvStartPrice;
            private TextView tvStatus, tvStartTime, tvEndTime;
            private Button btnEdit, btnDelete;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivProductImage = itemView.findViewById(R.id.iv_product_image);
                tvProductName = itemView.findViewById(R.id.tv_product_name);
                tvDescription = itemView.findViewById(R.id.tv_description);
                tvPrice = itemView.findViewById(R.id.tv_price);
                tvStartPrice = itemView.findViewById(R.id.tv_start_price);
                tvStatus = itemView.findViewById(R.id.tv_status);
                tvStartTime = itemView.findViewById(R.id.tv_start_time);
                tvEndTime = itemView.findViewById(R.id.tv_end_time);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
            
            public void bind(UserAuctionItem item) {
                Product product = item.getProduct();
                Auctions auction = item.getAuction();
                
                // Set product info
                tvProductName.setText(product.getProductName());
                tvDescription.setText(product.getDescribe());
                tvPrice.setText("Giá tham khảo: " + currencyFormatter.format(product.getPrice()));
                tvStartPrice.setText("Giá khởi điểm: " + currencyFormatter.format(auction.getStartPrice()));
                
                // Set time info
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                if (auction.getStartTime() != null) {
                    tvStartTime.setText("Bắt đầu: " + sdf.format(auction.getStartTime()));
                }
                if (auction.getEndTime() != null) {
                    tvEndTime.setText("Kết thúc: " + sdf.format(auction.getEndTime()));
                }
                
                // Set status
                String statusText = "Trạng thái: ";
                int statusColor = android.R.color.holo_blue_dark;
                switch (auction.getStatus()) {
                    case "pending":
                        statusText += "Chờ duyệt";
                        statusColor = android.R.color.holo_orange_dark;
                        break;
                    case "active":
                        statusText += "Đã duyệt";
                        statusColor = android.R.color.holo_green_dark;
                        break;
                    case "rejected":
                        statusText += "Đã từ chối";
                        statusColor = android.R.color.holo_red_dark;
                        break;
                    case "completed":
                        statusText += "Đã hoàn thành";
                        statusColor = android.R.color.holo_blue_dark;
                        break;
                    default:
                        statusText += auction.getStatus();
                        break;
                }
                tvStatus.setText(statusText);
                tvStatus.setTextColor(getResources().getColor(statusColor));
                
                // Load product image
                if (item.getImages() != null && !item.getImages().isEmpty()) {
                    loadProductImage(item.getImages().get(0).getPath());
                } else {
                    ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
                }
                
                // Set button visibility based on status
                if ("pending".equals(auction.getStatus())) {
                    // Pending: có thể sửa và xóa
                    btnEdit.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.VISIBLE);
                } else if ("active".equals(auction.getStatus())) {
                    // Active: chỉ có thể xóa (không thể sửa khi đã active)
                    btnEdit.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.VISIBLE);
                } else {
                    // Rejected/Completed: chỉ có thể xóa
                    btnEdit.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.VISIBLE);
                }
                
                // Set button listeners
                btnEdit.setOnClickListener(v -> editAuction(item));
                btnDelete.setOnClickListener(v -> deleteAuction(item));
            }
            
            private void loadProductImage(String imagePath) {
                if (imagePath != null) {
                    executorService.execute(() -> {
                        try {
                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                                runOnUiThread(() -> {
                                    if (bitmap != null) {
                                        ivProductImage.setImageBitmap(bitmap);
                                    } else {
                                        ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
                                    }
                                });
                            } else {
                                runOnUiThread(() -> {
                                    ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
                                });
                            }
                        } catch (Exception e) {
                            Log.e("UserAuction", "Error loading image: " + e.getMessage(), e);
                            runOnUiThread(() -> {
                                ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
                            });
                        }
                    });
                }
            }
        }
    }
    
    // Data class for user auction items
    public static class UserAuctionItem {
        private Auctions auction;
        private Product product;
        private User seller;
        private List<ProductImages> images;
        
        public UserAuctionItem(Auctions auction, Product product, User seller, List<ProductImages> images) {
            this.auction = auction;
            this.product = product;
            this.seller = seller;
            this.images = images;
        }
        
        // Getters
        public Auctions getAuction() { return auction; }
        public Product getProduct() { return product; }
        public User getSeller() { return seller; }
        public List<ProductImages> getImages() { return images; }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
