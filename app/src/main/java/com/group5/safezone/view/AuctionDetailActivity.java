package com.group5.safezone.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_AUCTION_ID = "auction_id";
    
    private int auctionId;
    private ExecutorService executorService;
    private NumberFormat currencyFormatter;
    private SessionManager sessionManager;
    
    // UI Components
    private ImageView ivProductImage;
    private TextView tvProductName, tvDescription, tvPrice, tvStartPrice;
    private TextView tvSellerName, tvStartTime, tvEndTime;
    private TextView tvStatus, tvCreatedAt;
    private Button btnApprove, btnReject, btnDelete, btnBack;
    private LinearLayout llImagesContainer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AuctionDetail", "onCreate() started");
        
        try {
            setContentView(R.layout.activity_auction_detail);
            Log.d("AuctionDetail", "Layout set successfully");
            
            // Check admin permission
            sessionManager = new SessionManager(this);
            if (!"ADMIN".equals(sessionManager.getUserRole())) {
                Log.w("AuctionDetail", "Non-admin user trying to access auction detail");
                Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            auctionId = getIntent().getIntExtra(EXTRA_AUCTION_ID, -1);
            Log.d("AuctionDetail", "Auction ID: " + auctionId);
            if (auctionId == -1) {
                Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            executorService = Executors.newFixedThreadPool(4);
            currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            
            initViews();
            loadAuctionDetails();
            
        } catch (Exception e) {
            Log.e("AuctionDetail", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initViews() {
        try {
            Log.d("AuctionDetail", "Initializing views");
            
            ivProductImage = findViewById(R.id.iv_product_image);
            tvProductName = findViewById(R.id.tv_product_name);
            tvDescription = findViewById(R.id.tv_description);
            tvPrice = findViewById(R.id.tv_price);
            tvStartPrice = findViewById(R.id.tv_start_price);
            tvSellerName = findViewById(R.id.tv_seller_name);
            tvStartTime = findViewById(R.id.tv_start_time);
            tvEndTime = findViewById(R.id.tv_end_time);
            tvStatus = findViewById(R.id.tv_status);
            tvCreatedAt = findViewById(R.id.tv_created_at);
            btnApprove = findViewById(R.id.btn_approve);
            btnReject = findViewById(R.id.btn_reject);
            btnDelete = findViewById(R.id.btn_delete);
            btnBack = findViewById(R.id.btn_back);
            llImagesContainer = findViewById(R.id.ll_images_container);
            
            // Check for null views
            if (btnBack == null) {
                Log.e("AuctionDetail", "btnBack is null!");
                throw new RuntimeException("btnBack not found in layout");
            }
            if (tvProductName == null) {
                Log.e("AuctionDetail", "tvProductName is null!");
                throw new RuntimeException("tvProductName not found in layout");
            }
            
            Log.d("AuctionDetail", "All views initialized successfully");
            
            btnBack.setOnClickListener(v -> finish());
            btnApprove.setOnClickListener(v -> approveAuction());
            btnReject.setOnClickListener(v -> rejectAuction());
            btnDelete.setOnClickListener(v -> deleteAuction());
            
        } catch (Exception e) {
            Log.e("AuctionDetail", "Error in initViews: " + e.getMessage(), e);
            throw e; // Re-throw to be caught by onCreate
        }
    }
    
    private void loadAuctionDetails() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Load auction
                Auctions auction = db.auctionsDao().getAuctionById(auctionId);
                if (auction == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy thông tin đấu giá", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                
                // Load product
                Product product = db.productDao().getProductById(auction.getProductId());
                if (product == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                
                // Load seller
                User seller = db.userDao().getUserById(auction.getSellerUserId());
                if (seller == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy thông tin người bán", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                
                // Load images
                List<ProductImages> images = db.productImagesDao().getImagesByProductId(auction.getProductId());
                
                runOnUiThread(() -> {
                    displayAuctionDetails(auction, product, seller, images);
                });
                
            } catch (Exception e) {
                Log.e("AuctionDetail", "Error loading auction details: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void displayAuctionDetails(Auctions auction, Product product, User seller, List<ProductImages> images) {
        // Set product info
        tvProductName.setText(product.getProductName());
        tvDescription.setText(product.getDescribe());
        tvPrice.setText("Giá tham khảo: " + currencyFormatter.format(product.getPrice()));
        tvStartPrice.setText("Giá khởi điểm: " + currencyFormatter.format(auction.getStartPrice()));
        
        // Set seller info
        tvSellerName.setText("Người bán: " + seller.getUserName() + " - " + seller.getEmail());
        
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
        switch (auction.getStatus()) {
            case "pending":
                statusText += "Chờ duyệt";
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "active":
                statusText += "Đang hoạt động";
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "rejected":
                statusText += "Đã từ chối";
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            case "completed":
                statusText += "Đã hoàn thành";
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            default:
                statusText += auction.getStatus();
                break;
        }
        tvStatus.setText(statusText);
        
        // Set created at
        if (product.getCreatedAt() != null) {
            tvCreatedAt.setText("Ngày tạo: " + sdf.format(product.getCreatedAt()));
        }
        
        // Load and display images
        if (images != null && !images.isEmpty()) {
            loadProductImages(images);
        } else {
            ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
        }
        
        // Show/hide action buttons based on status
        if ("pending".equals(auction.getStatus())) {
            // Pending: có thể duyệt, từ chối, xóa
            btnApprove.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        } else if ("active".equals(auction.getStatus())) {
            // Active: có thể từ chối hoặc xóa (không thể duyệt nữa)
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        } else if ("rejected".equals(auction.getStatus())) {
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
    
    private void loadProductImages(List<ProductImages> images) {
        // Load main image
        if (!images.isEmpty()) {
            loadImageFromPath(images.get(0).getPath(), ivProductImage);
        }
        
        // Load additional images
        llImagesContainer.removeAllViews();
        for (int i = 1; i < Math.min(images.size(), 5); i++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(4, 4, 4, 4);
            
            loadImageFromPath(images.get(i).getPath(), imageView);
            llImagesContainer.addView(imageView);
        }
    }
    
    private void loadImageFromPath(String imagePath, ImageView imageView) {
        if (imagePath != null) {
            executorService.execute(() -> {
                try {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        runOnUiThread(() -> {
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            } else {
                                imageView.setImageResource(R.drawable.ic_image_placeholder);
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            imageView.setImageResource(R.drawable.ic_image_placeholder);
                        });
                    }
                } catch (Exception e) {
                    Log.e("AuctionDetail", "Error loading image: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        imageView.setImageResource(R.drawable.ic_image_placeholder);
                    });
                }
            });
        }
    }
    
    private void approveAuction() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Get current auction and product
                Auctions auction = db.auctionsDao().getAuctionById(auctionId);
                Product product = db.productDao().getProductById(auction.getProductId());
                
                if (auction != null && product != null) {
                    // Approve auction
                    auction.setStatus("active");
                    product.setIsAdminCheck(true);
                    product.setStatus("active");
                    
                    db.auctionsDao().update(auction);
                    db.productDao().update(product);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã duyệt bài đấu giá thành công", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
                
            } catch (Exception e) {
                Log.e("AuctionDetail", "Error approving auction: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi duyệt: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void rejectAuction() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Get current auction and product
                Auctions auction = db.auctionsDao().getAuctionById(auctionId);
                Product product = db.productDao().getProductById(auction.getProductId());
                
                if (auction != null && product != null) {
                    // Reject auction
                    auction.setStatus("rejected");
                    product.setStatus("rejected");
                    
                    db.auctionsDao().update(auction);
                    db.productDao().update(product);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã từ chối bài đấu giá", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
                
            } catch (Exception e) {
                Log.e("AuctionDetail", "Error rejecting auction: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi từ chối: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void deleteAuction() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Get current auction and product
                Auctions auction = db.auctionsDao().getAuctionById(auctionId);
                Product product = db.productDao().getProductById(auction.getProductId());
                List<ProductImages> images = db.productImagesDao().getImagesByProductId(auction.getProductId());
                
                if (auction != null && product != null) {
                    // Delete images first
                    if (images != null) {
                        for (ProductImages image : images) {
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
                    db.auctionsDao().delete(auction);
                    db.productDao().delete(product);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã xóa bài đấu giá", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
                
            } catch (Exception e) {
                Log.e("AuctionDetail", "Error deleting auction: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
