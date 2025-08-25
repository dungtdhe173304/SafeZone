package com.group5.safezone.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.group5.safezone.R;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.repository.AuctionRepository;
import com.group5.safezone.viewmodel.AuctionViewModel;
import com.group5.safezone.viewmodel.UserViewModel;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionResultActivity extends AppCompatActivity {

    public static final String EXTRA_AUCTION_ID = "auction_id";
    public static final String EXTRA_WINNER_NAME = "winner_name";
    public static final String EXTRA_WINNING_BID = "winning_bid";
    public static final String EXTRA_PRODUCT_NAME = "product_name";
    public static final String EXTRA_WINNER_ID = "winner_id";

    private int auctionId;
    private String winnerName;
    private double winningBid;
    private String productName;
    private int winnerId;

    // UI Components
    private ImageView ivProductImage;
    private TextView tvProductName, tvWinnerName, tvWinningBid, tvAuctionId;
    private Button btnBackToHome, btnViewHistory, btnPayment;

    // Data
    private SessionManager sessionManager;
    private AuctionViewModel auctionViewModel;
    private UserViewModel userViewModel;
    private ExecutorService executorService;
    private User currentUser;
    private boolean isCurrentUserWinner = false;

    // Formatters
    private NumberFormat currencyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction_result);

        try {
            // Get intent data
            auctionId = getIntent().getIntExtra(EXTRA_AUCTION_ID, -1);
            winnerName = getIntent().getStringExtra(EXTRA_WINNER_NAME);
            winningBid = getIntent().getDoubleExtra(EXTRA_WINNING_BID, 0.0);
            productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
            winnerId = getIntent().getIntExtra(EXTRA_WINNER_ID, -1);

            if (auctionId == -1) {
                finish();
                return;
            }

            initViews();
            initData();
            setupFormatters();
            setupClickListeners();
            checkIfCurrentUserIsWinner();
            displayResult();

        } catch (Exception e) {
            finish();
        }
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvWinnerName = findViewById(R.id.tv_winner_name);
        tvWinningBid = findViewById(R.id.tv_winning_bid);
        tvAuctionId = findViewById(R.id.tv_auction_id);
        btnBackToHome = findViewById(R.id.btn_back_to_home);
        btnViewHistory = findViewById(R.id.btn_view_history);
        btnPayment = findViewById(R.id.btn_payment);
    }

    private void initData() {
        sessionManager = new SessionManager(this);
        auctionViewModel = new AuctionViewModel(getApplication());
        userViewModel = new UserViewModel(getApplication());
        executorService = Executors.newFixedThreadPool(4);
    }

    private void setupFormatters() {
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    private void setupClickListeners() {
        btnBackToHome.setOnClickListener(v -> {
            // Go back to main activity
            finish();
        });

        btnViewHistory.setOnClickListener(v -> {
            // Navigate to auction room to view bid history
            Intent intent = new Intent(this, AuctionRoomActivity.class);
            intent.putExtra(AuctionRoomActivity.EXTRA_AUCTION_ID, auctionId);
            startActivity(intent);
        });

        btnPayment.setOnClickListener(v -> {
            if (isCurrentUserWinner) {
                showPaymentConfirmation();
            } else {
                Toast.makeText(this, "Chỉ người thắng cuộc mới có thể thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfCurrentUserIsWinner() {
        executorService.execute(() -> {
            try {
                // Get current user
                int currentUserId = sessionManager.getUserId();
                if (currentUserId > 0) {
                    AuctionRepository repository = auctionViewModel.getRepository();
                    User user = repository.getDatabase().userDao().getUserById(currentUserId);
                    
                    runOnUiThread(() -> {
                        currentUser = user;
                        
                        // Check if current user is the winner
                        if (currentUser != null && winnerId > 0 && 
                            currentUser.getId() == winnerId) {
                            isCurrentUserWinner = true;
                            btnPayment.setVisibility(android.view.View.VISIBLE);
                        } else {
                            isCurrentUserWinner = false;
                            btnPayment.setVisibility(android.view.View.GONE);
                        }
                    });
                } else {
                    // User not logged in
                    runOnUiThread(() -> {
                        isCurrentUserWinner = false;
                        btnPayment.setVisibility(android.view.View.GONE);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AuctionResultActivity.this, 
                        "Lỗi khi kiểm tra thông tin người dùng: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    isCurrentUserWinner = false;
                    btnPayment.setVisibility(android.view.View.GONE);
                });
            }
        });
    }

    private void showPaymentConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("💳 Xác nhận thanh toán");
        
        String message = String.format("Bạn có chắc chắn muốn thanh toán?\n\nSản phẩm: %s\nSố tiền: %s\nSố dư hiện tại: %s", 
            productName != null ? productName : "Sản phẩm",
            currencyFormatter.format(winningBid),
            currentUser != null ? currencyFormatter.format(currentUser.getBalance()) : "0 ₫");
        
        // Add warning if balance is insufficient
        if (currentUser != null && currentUser.getBalance() < winningBid) {
            message += "\n\n⚠️ CẢNH BÁO: Số dư không đủ để thanh toán!";
        }
        
        builder.setMessage(message);
        builder.setPositiveButton("Thanh toán", (dialog, which) -> {
            processPayment();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
        });
        
        builder.show();
    }

    private void processPayment() {
        if (currentUser == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user has enough balance
        if (currentUser.getBalance() < winningBid) {
            Toast.makeText(this, "Số dư không đủ để thanh toán!", Toast.LENGTH_LONG).show();
            return;
        }

        // Disable payment button to prevent multiple clicks
        btnPayment.setEnabled(false);

        executorService.execute(() -> {
            try {
                AuctionRepository repository = auctionViewModel.getRepository();
                
                // Deduct money from user balance
                double newBalance = currentUser.getBalance() - winningBid;
                currentUser.setBalance(newBalance);
                repository.getDatabase().userDao().updateUser(currentUser);
                
                // Update session balance
                sessionManager.updateBalance(newBalance);
                
                runOnUiThread(() -> {
                    // Show success message
                    Toast.makeText(AuctionResultActivity.this, 
                        "🎉 Thanh toán thành công! Số dư còn lại: " + currencyFormatter.format(newBalance), 
                        Toast.LENGTH_LONG).show();
                    
                    // Hide payment button
                    btnPayment.setVisibility(android.view.View.GONE);
                    
                    // Update UI to show payment completed
                    updateUIAfterPayment();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AuctionResultActivity.this, 
                        "Lỗi khi xử lý thanh toán: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    btnPayment.setEnabled(true);
                });
            }
        });
    }

    private void updateUIAfterPayment() {
        // Update winner info to show payment completed
        tvWinnerName.setText(winnerName + " (Đã thanh toán)");
        tvWinnerName.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        
        // Update winning bid to show payment completed
        tvWinningBid.setText(currencyFormatter.format(winningBid) + " (Đã thanh toán)");
        tvWinningBid.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        
        // Show payment completed message
        Toast.makeText(this, "🎉 Chúc mừng! Bạn đã sở hữu sản phẩm này!", Toast.LENGTH_LONG).show();
    }

    private void displayResult() {
        try {
            // Set product name
            if (productName != null) {
                tvProductName.setText(productName);
            } else {
                tvProductName.setText("Sản phẩm");
            }

            // Set winner name
            if (winnerName != null && !winnerName.equals("Không có")) {
                tvWinnerName.setText(winnerName);
                tvWinnerName.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvWinnerName.setText("Không có người thắng cuộc");
                tvWinnerName.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Set winning bid
            if (winningBid > 0) {
                tvWinningBid.setText(currencyFormatter.format(winningBid));
                tvWinningBid.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvWinningBid.setText("Không có giá trúng");
                tvWinningBid.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Set auction ID
            tvAuctionId.setText("Mã phiên đấu giá: " + auctionId);

        } catch (Exception e) {
            // Handle error silently
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
