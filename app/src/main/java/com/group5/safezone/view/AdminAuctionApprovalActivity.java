package com.group5.safezone.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.viewmodel.AuctionViewModel;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminAuctionApprovalActivity extends AppCompatActivity {
    
    private AuctionViewModel viewModel;
    private SessionManager sessionManager;
    private RecyclerView recyclerView;
    private AuctionApprovalAdapter adapter;
    private List<AuctionApprovalItem> pendingAuctions = new ArrayList<>();
    private ExecutorService executorService;
    private NumberFormat currencyFormatter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_auction_approval);
        
        // Khởi tạo ViewModel và SessionManager
        viewModel = new ViewModelProvider(this).get(AuctionViewModel.class);
        sessionManager = new SessionManager(this);
        executorService = Executors.newFixedThreadPool(4);
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        // Kiểm tra quyền admin
        if (!"ADMIN".equals(sessionManager.getUserRole())) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadPendingAuctions();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AuctionApprovalAdapter();
        recyclerView.setAdapter(adapter);
    }
    
    private void loadPendingAuctions() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Load tất cả auctions (không chỉ pending) để admin có thể quản lý
                List<Auctions> allAuctions = db.auctionsDao().getAllAuctions();
                List<AuctionApprovalItem> items = new ArrayList<>();
                
                for (Auctions auction : allAuctions) {
                    Product product = db.productDao().getProductById(auction.getProductId());
                    User seller = db.userDao().getUserById(auction.getSellerUserId());
                    List<ProductImages> images = db.productImagesDao().getImagesByProductId(auction.getProductId());
                    
                    if (product != null && seller != null) {
                        AuctionApprovalItem item = new AuctionApprovalItem();
                        item.setAuction(auction);
                        item.setProduct(product);
                        item.setSeller(seller);
                        item.setImages(images);
                        items.add(item);
                    }
                }
                
                runOnUiThread(() -> {
                    pendingAuctions.clear();
                    pendingAuctions.addAll(items);
                    adapter.notifyDataSetChanged();
                    
                    if (pendingAuctions.isEmpty()) {
                        Toast.makeText(this, "Không có bài đấu giá nào", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                Log.e("AdminAuction", "Error loading auctions: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void approveAuction(AuctionApprovalItem item, boolean approved) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                if (approved) {
                    // Approve auction
                    item.getAuction().setStatus("active");
                    item.getProduct().setIsAdminCheck(true);
                    item.getProduct().setStatus("active");
                    
                    db.auctionsDao().update(item.getAuction());
                    db.productDao().update(item.getProduct());
                    
                    runOnUiThread(() -> {
                        String message = "rejected".equals(item.getAuction().getStatus()) ? 
                            "Đã mở lại bài đấu giá thành công" : 
                            "Đã duyệt bài đấu giá thành công";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    });
                } else {
                    // Reject auction
                    item.getAuction().setStatus("rejected");
                    item.getProduct().setStatus("rejected");
                    
                    db.auctionsDao().update(item.getAuction());
                    db.productDao().update(item.getProduct());
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã từ chối bài đấu giá", Toast.LENGTH_LONG).show();
                    });
                }
                
                // Update the item in the list instead of refreshing
                runOnUiThread(() -> {
                    // Update the auction status in the list
                    for (int i = 0; i < pendingAuctions.size(); i++) {
                        if (pendingAuctions.get(i).getAuction().getId() == item.getAuction().getId()) {
                            pendingAuctions.get(i).getAuction().setStatus(approved ? "active" : "rejected");
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                });
                
            } catch (Exception e) {
                Log.e("AdminAuction", "Error approving auction: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi xử lý: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void deleteAuction(AuctionApprovalItem item) {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Delete images first
                if (item.getImages() != null) {
                    for (ProductImages image : item.getImages()) {
                        // Delete image file
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
                    loadPendingAuctions();
                });
                
            } catch (Exception e) {
                Log.e("AdminAuction", "Error deleting auction: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    // Adapter for RecyclerView
    private class AuctionApprovalAdapter extends RecyclerView.Adapter<AuctionApprovalAdapter.ViewHolder> {
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_auction_approval, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AuctionApprovalItem item = pendingAuctions.get(position);
            holder.bind(item);
        }
        
        @Override
        public int getItemCount() {
            return pendingAuctions.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivProductImage;
            private TextView tvProductName, tvDescription, tvPrice, tvStartPrice;
            private TextView tvSellerName, tvStartTime, tvEndTime;
            private Button btnApprove, btnReject, btnDelete, btnViewDetails;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivProductImage = itemView.findViewById(R.id.iv_product_image);
                tvProductName = itemView.findViewById(R.id.tv_product_name);
                tvDescription = itemView.findViewById(R.id.tv_description);
                tvPrice = itemView.findViewById(R.id.tv_price);
                tvStartPrice = itemView.findViewById(R.id.tv_start_price);
                tvSellerName = itemView.findViewById(R.id.tv_seller_name);
                tvStartTime = itemView.findViewById(R.id.tv_start_time);
                tvEndTime = itemView.findViewById(R.id.tv_end_time);
                btnApprove = itemView.findViewById(R.id.btn_approve);
                btnReject = itemView.findViewById(R.id.btn_reject);
                btnDelete = itemView.findViewById(R.id.btn_delete);
                btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            }
            
            public void bind(AuctionApprovalItem item) {
                Product product = item.getProduct();
                Auctions auction = item.getAuction();
                User seller = item.getSeller();
                
                // Set product info
                tvProductName.setText(product.getProductName());
                tvDescription.setText(product.getDescribe());
                tvPrice.setText("Giá tham khảo: " + currencyFormatter.format(product.getPrice()));
                tvStartPrice.setText("Giá khởi điểm: " + currencyFormatter.format(auction.getStartPrice()));
                tvSellerName.setText("Người bán: " + seller.getUserName());
                
                // Set time info
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                if (auction.getStartTime() != null) {
                    tvStartTime.setText("Bắt đầu: " + sdf.format(auction.getStartTime()));
                }
                if (auction.getEndTime() != null) {
                    tvEndTime.setText("Kết thúc: " + sdf.format(auction.getEndTime()));
                }
                
                // Set status info
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
                
                // Set status text and color
                TextView tvStatus = itemView.findViewById(R.id.tv_status);
                tvStatus.setText(statusText);
                tvStatus.setTextColor(getResources().getColor(statusColor));
                
                // Load product image
                if (item.getImages() != null && !item.getImages().isEmpty()) {
                    loadProductImage(item.getImages().get(0).getPath());
                } else {
                    ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
                }
                
                // Set button visibility based on status
                setupButtonsByStatus(auction.getStatus());
                
                // Set button listeners
                btnApprove.setOnClickListener(v -> showApprovalDialog(item, true));
                btnReject.setOnClickListener(v -> showApprovalDialog(item, false));
                btnDelete.setOnClickListener(v -> showDeleteDialog(item));
                btnViewDetails.setOnClickListener(v -> viewAuctionDetails(item));
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
                            Log.e("AdminAuction", "Error loading image: " + e.getMessage(), e);
                            runOnUiThread(() -> {
                                ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
                            });
                        }
                    });
                }
            }
            
            private void showApprovalDialog(AuctionApprovalItem item, boolean approved) {
                String action, message;
                if (approved) {
                    if ("rejected".equals(item.getAuction().getStatus())) {
                        action = "mở lại";
                        message = "Bạn có chắc chắn muốn mở lại bài đấu giá này?";
                    } else {
                        action = "duyệt";
                        message = "Bạn có chắc chắn muốn duyệt bài đấu giá này?";
                    }
                } else {
                    action = "từ chối";
                    message = "Bạn có chắc chắn muốn từ chối bài đấu giá này?";
                }
                
                new AlertDialog.Builder(AdminAuctionApprovalActivity.this)
                    .setTitle("Xác nhận " + action)
                    .setMessage(message)
                    .setPositiveButton(action, (dialog, which) -> {
                        approveAuction(item, approved);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            }
            
            private void showDeleteDialog(AuctionApprovalItem item) {
                new AlertDialog.Builder(AdminAuctionApprovalActivity.this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa bài đấu giá này? Hành động này không thể hoàn tác.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        deleteAuction(item);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            }
            
            private void viewAuctionDetails(AuctionApprovalItem item) {
                Intent intent = new Intent(AdminAuctionApprovalActivity.this, AuctionDetailActivity.class);
                intent.putExtra(AuctionDetailActivity.EXTRA_AUCTION_ID, item.getAuction().getId());
                startActivity(intent);
            }
            
            private void setupButtonsByStatus(String status) {
                if ("pending".equals(status)) {
                    // Pending: có thể duyệt, từ chối, xóa
                    btnApprove.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.VISIBLE);
                } else if ("active".equals(status)) {
                    // Active: có thể từ chối hoặc xóa (không thể duyệt nữa)
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.VISIBLE);
                } else if ("rejected".equals(status)) {
                    // Rejected: có thể approve lại hoặc xóa
                    btnApprove.setVisibility(View.VISIBLE);
                    btnApprove.setText("Mở lại");
                    btnReject.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.VISIBLE);
                } else {
                    // Completed hoặc status khác: chỉ có thể xem
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.GONE);
                }
            }
        }
    }
    
    // Data class for auction approval items
    public static class AuctionApprovalItem {
        private Auctions auction;
        private Product product;
        private User seller;
        private List<ProductImages> images;
        
        // Getters and setters
        public Auctions getAuction() { return auction; }
        public void setAuction(Auctions auction) { this.auction = auction; }
        
        public Product getProduct() { return product; }
        public void setProduct(Product product) { this.product = product; }
        
        public User getSeller() { return seller; }
        public void setSeller(User seller) { this.seller = seller; }
        
        public List<ProductImages> getImages() { return images; }
        public void setImages(List<ProductImages> images) { this.images = images; }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
