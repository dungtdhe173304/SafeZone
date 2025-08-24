package com.group5.safezone.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.adapter.ProductAdapter.ProductImagesAdapter;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.dao.OrderDao;
import com.group5.safezone.model.dao.ProductDao;
import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.model.dao.TransactionsDao;
import com.group5.safezone.model.entity.Order;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.model.entity.Transactions;
import com.group5.safezone.view.Wallet.WalletActivity;
import com.group5.safezone.view.PurchaseHistoryActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BuyProductActivity extends AppCompatActivity {
    
    // UI Components
    private ImageButton btnBack;
    private RecyclerView rvProductImages;
    private TextView tvProductName, tvProductDescription, tvSellerInfo, tvProductPrice, tvFeeInfo;
    private TextView tvTotalPrice, tvUserBalance;
    private Button btnDeposit, btnBuyNow;
    
    // Data
    private Product product;
    private List<ProductImages> productImages;
    private String sellerUsername;
    private double currentPrice;
    private double userBalance;
    private int currentUserId;
    private boolean isPurchasing = false;
    
    // Database
    private ProductDao productDao;
    private UserDao userDao;
    private OrderDao orderDao;
    private TransactionsDao transactionsDao;
    private ExecutorService executor;
    private SessionManager sessionManager;
    
    // Constants
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_PRODUCT_PRICE = "product_price";
    public static final String EXTRA_SELLER_USERNAME = "seller_username";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_product);
        
        initViews();
        setupDatabase();
        loadProductData();
        setupListeners();
        loadUserBalance();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvProductImages = findViewById(R.id.rvProductImages);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        tvSellerInfo = findViewById(R.id.tvSellerInfo);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvFeeInfo = findViewById(R.id.tvFeeInfo);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvUserBalance = findViewById(R.id.tvUserBalance);
        btnDeposit = findViewById(R.id.btnDeposit);
        btnBuyNow = findViewById(R.id.btnBuyNow);
    }
    
    private void setupDatabase() {
        AppDatabase db = AppDatabase.getDatabase(this);
        productDao = db.productDao();
        userDao = db.userDao();
        orderDao = db.orderDao();
        transactionsDao = db.transactionsDao();
        executor = Executors.newSingleThreadExecutor();
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
    }
    
    private void loadProductData() {
        String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        currentPrice = getIntent().getDoubleExtra(EXTRA_PRODUCT_PRICE, 0);
        sellerUsername = getIntent().getStringExtra(EXTRA_SELLER_USERNAME);
        
        if (productId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Load product from database
        executor.execute(() -> {
            Product dbProduct = productDao.getProductById(productId);
            List<ProductImages> dbImages = AppDatabase.getDatabase(this).productImagesDao().getImagesByProductId(productId);
            
            runOnUiThread(() -> {
                if (dbProduct != null) {
                    this.product = dbProduct;
                    this.productImages = dbImages;
                    displayProductInfo();
                    updateTotalPrice();
                } else {
                    Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }
    
    private void displayProductInfo() {
        if (product == null) return;
        
        tvProductName.setText(product.getProductName());
        tvProductDescription.setText(product.getDescribe() != null ? product.getDescribe() : "Không có mô tả");
        tvSellerInfo.setText("Người bán: " + sellerUsername);
        tvProductPrice.setText("Giá: " + formatPrice(product.getPrice()));
        
        // Display fee info
        String feeInfo = product.getFee() == 1 ? "Phí: Người bán chịu" : "Phí: Người mua chịu";
        tvFeeInfo.setText(feeInfo);
        
        // Setup product images
        if (productImages != null && !productImages.isEmpty()) {
            ProductImagesAdapter adapter = new ProductImagesAdapter();
            List<String> imagePaths = new ArrayList<>();
            for (ProductImages img : productImages) {
                if (img.getPath() != null) {
                    imagePaths.add(img.getPath());
                }
            }
            adapter.updateImages(productImages);
            rvProductImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvProductImages.setAdapter(adapter);
        }
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnDeposit.setOnClickListener(v -> {
            Intent intent = new Intent(this, WalletActivity.class);
            startActivity(intent);
        });
        
        btnBuyNow.setOnClickListener(v -> showPurchaseConfirmationDialog());
    }
    
    private void updateTotalPrice() {
        if (product != null) {
            double total = product.getPrice() * 1; // Quantity is always 1
            tvTotalPrice.setText(formatPrice(total));
        }
    }
    
    private void loadUserBalance() {
        executor.execute(() -> {
            User user = userDao.getUserById(currentUserId);
            if (user != null) {
                userBalance = user.getBalance() != null ? user.getBalance() : 0;
                runOnUiThread(() -> tvUserBalance.setText(formatPrice(userBalance)));
            }
        });
    }
    
    private void showPurchaseConfirmationDialog() {
        if (product == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double totalCost = product.getPrice() * 1; // Quantity is always 1
        String confirmMessage = "Bạn có chắc chắn muốn mua sản phẩm này?\n\n" +
                "Sản phẩm: " + product.getProductName() + "\n" +
                "Người bán: " + sellerUsername + "\n" +
                "Tổng tiền: " + formatPrice(totalCost) + "\n\n" +
                "Số dư hiện tại: " + formatPrice(userBalance) + "\n\n" +
                "🎥 LƯU Ý QUAN TRỌNG:\n" +
                "• Hãy chụp ảnh/quay video sản phẩm khi nhận hàng\n" +
                "• Kiểm tra kỹ chất lượng và thông tin sản phẩm\n" +
                "• Lưu lại bằng chứng để phòng trường hợp tranh chấp\n" +
                "• SafeZone sẽ hỗ trợ giải quyết nếu có vấn đề";
        
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Xác nhận mua hàng")
            .setMessage(confirmMessage)
            .setIcon(R.drawable.ic_shopping_bag)
            .setPositiveButton("Tôi đã hiểu, tiếp tục mua", (dialog, which) -> {
                validateAndBuy();
            })
            .setNegativeButton("Hủy", (dialog, which) -> {
                // User cancelled, do nothing
            })
            .setCancelable(false)
            .show();
    }

    private void validateAndBuy() {
        if (product == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Prevent double purchase
        if (isPurchasing) {
            Toast.makeText(this, "Đang xử lý đơn hàng, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        isPurchasing = true;
        btnBuyNow.setEnabled(false);
        btnBuyNow.setText("Đang xử lý...");
        
        // Check if product info has changed
        executor.execute(() -> {
            Product currentProduct = productDao.getProductById(product.getId());
            
            if (currentProduct == null) {
                runOnUiThread(() -> {
                    resetPurchaseState();
                    Toast.makeText(this, "Sản phẩm không còn tồn tại", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
            
            // Check if price or other info has changed
            if (!currentProduct.getPrice().equals(product.getPrice()) || 
                !currentProduct.getProductName().equals(product.getProductName())) {
                runOnUiThread(() -> {
                    resetPurchaseState();
                    showProductChangedDialog();
                });
                return;
            }
            
            // Check user balance
            double totalCost = product.getPrice() * 1; // Quantity is always 1
            if (userBalance < totalCost) {
                runOnUiThread(() -> {
                    resetPurchaseState();
                    showInsufficientBalanceDialog(totalCost);
                });
                return;
            }
            
            // Proceed with purchase
            runOnUiThread(() -> {
                processPurchase(totalCost);
            });
        });
    }
    
    private void showProductChangedDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Thông tin sản phẩm đã thay đổi")
            .setMessage("Sản phẩm đã được chủ cập nhật lại. Vui lòng kiểm tra kỹ thông tin trước khi mua.")
            .setPositiveButton("Xem lại", (dialog, which) -> {
                // Reload product data
                loadProductData();
            })
            .setNegativeButton("Hủy", (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    private void showInsufficientBalanceDialog(double requiredAmount) {
        new AlertDialog.Builder(this)
            .setTitle("Số dư không đủ")
            .setMessage("Bạn cần " + formatPrice(requiredAmount) + " để mua sản phẩm này. Số dư hiện tại: " + formatPrice(userBalance))
            .setPositiveButton("Nạp tiền", (dialog, which) -> {
                Intent intent = new Intent(this, WalletActivity.class);
                startActivity(intent);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void processPurchase(double totalCost) {
        // Create order
        Order order = new Order();
        order.setProductId(product.getId());
        order.setUserId(currentUserId);
        order.setPrice(totalCost);
        order.setQuantity(1); // Quantity is always 1
        order.setStatus("chờ người mua xác nhận nhận");
        order.setNote(getNoteText());
        order.setOrderDate(new Date());
        
        // Save order and update balance
        executor.execute(() -> {
            try {
                // Insert order and get the generated ID
                long orderId = orderDao.insert(order);
                
                // Create transaction record for purchase
                Transactions purchaseTransaction = new Transactions();
                purchaseTransaction.setUserId(currentUserId);
                purchaseTransaction.setRelatedUserId(product.getUserId()); // Seller ID
                purchaseTransaction.setOrderId((int) orderId);
                purchaseTransaction.setTransactionType("Mua sản phẩm");
                purchaseTransaction.setAmount(totalCost);
                purchaseTransaction.setStatus("Hoàn thành");
                purchaseTransaction.setDescription("Mua sản phẩm: " + product.getProductName() + " từ " + sellerUsername);
                
                // Insert transaction
                transactionsDao.insert(purchaseTransaction);
                
                // Update user balance
                User user = userDao.getUserById(currentUserId);
                if (user != null) {
                    user.setBalance(user.getBalance() - totalCost);
                    userDao.updateUser(user);
                    
                    // Update UI
                    runOnUiThread(() -> {
                        userBalance = user.getBalance();
                        tvUserBalance.setText(formatPrice(userBalance));
                        
                        // Show success dialog
                        showPurchaseSuccessDialog(orderId);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    resetPurchaseState();
                    Toast.makeText(this, "Lỗi khi tạo đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showPurchaseSuccessDialog(long orderId) {
        String successMessage = "Đơn hàng #" + orderId + " đã được tạo với trạng thái 'chờ người mua xác nhận nhận'.\n\n" +
                "📋 NHẮC NHỞ QUAN TRỌNG:\n" +
                "• Khi nhận hàng, hãy chụp ảnh/quay video\n" +
                "• Kiểm tra kỹ sản phẩm trước khi xác nhận\n" +
                "• Lưu giữ bằng chứng để bảo vệ quyền lợi\n" +
                "• Liên hệ hỗ trợ nếu có vấn đề\n\n" +
                "🔒 SafeZone cam kết bảo vệ người mua!";
        
        new AlertDialog.Builder(this)
            .setTitle("🎉 Mua hàng thành công!")
            .setMessage(successMessage)
            .setPositiveButton("Xem lịch sử mua hàng", (dialog, which) -> {
                // Navigate to purchase history
                Intent intent = new Intent(this, PurchaseHistoryActivity.class);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Đóng", (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    private String getNoteText() {
        TextView etNote = findViewById(R.id.etNote);
        return etNote.getText().toString().trim();
    }
    
    private String formatPrice(double price) {
        return String.format("%,.0f VNĐ", price);
    }
    
    private void resetPurchaseState() {
        isPurchasing = false;
        btnBuyNow.setEnabled(true);
        btnBuyNow.setText("MUA NGAY");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload user balance when returning from wallet
        loadUserBalance();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
