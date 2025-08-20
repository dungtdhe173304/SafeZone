package com.group5.safezone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.group5.safezone.R;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.AuctionRegistrations;
import com.group5.safezone.model.ui.AuctionItemUiModel;
import com.group5.safezone.viewmodel.AuctionViewModel;
import com.group5.safezone.viewmodel.UserViewModel;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.view.base.BaseActivity;

public class AuctionRegistrationActivity extends BaseActivity {
    
    public static final String EXTRA_AUCTION_ID = "auction_id";
    
    private AuctionViewModel viewModel;
    private UserViewModel userViewModel;
    private SessionManager sessionManager;
    private int auctionId;
    private AuctionItemUiModel auctionItem;
    
    private TextView tvProductName;
    private TextView tvStartPrice;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private TextView tvCurrentBalance;
    private TextView tvUserName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvSellerName;
    private TextView tvSellerEmail;
    private TextView tvSellerPhone;
    private TextView tvRequiredDeposit;
    private TextView tvStatus;
    private Button btnRegister;
    private Button btnCancel;
    private Button btnJoinAuction;
    private View progressBar;
    private TextView tvHelloUserHeader;
    private TextView tvBalancePillHeader;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction_registration);
        
        // Khởi tạo ViewModel và SessionManager
        viewModel = new ViewModelProvider(this).get(AuctionViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        sessionManager = new SessionManager(this);
        
        // Lấy dữ liệu từ Intent
        auctionId = getIntent().getIntExtra(EXTRA_AUCTION_ID, -1);
        
        if (auctionId == -1) {
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupObservers();
        loadUserData();
        // Load dữ liệu phiên đấu giá và trạng thái đăng ký
        int userId = sessionManager.getUserId();
        viewModel.loadItem(auctionId, userId);
        checkRegistrationStatus();
        // enable footer navigation
        setupFooter();
    }
    
    private void initViews() {
        tvProductName = findViewById(R.id.tv_product_name);
        tvStartPrice = findViewById(R.id.tv_start_price);
        tvCurrentBalance = findViewById(R.id.tv_current_balance);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        tvUserName = findViewById(R.id.tv_user_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvSellerEmail = findViewById(R.id.tv_seller_email);
        tvSellerPhone = findViewById(R.id.tv_seller_phone);
        tvRequiredDeposit = findViewById(R.id.tv_required_deposit);
        tvStatus = findViewById(R.id.tv_status);
        btnRegister = findViewById(R.id.btn_register);
        btnCancel = findViewById(R.id.btn_cancel);
        btnJoinAuction = findViewById(R.id.btn_join_auction);
        progressBar = findViewById(R.id.progress_bar);
        // header views
        tvHelloUserHeader = findViewById(R.id.tvHelloUser);
        tvBalancePillHeader = findViewById(R.id.tvBalancePill);
        
        // Thông tin hiển thị sẽ được cập nhật khi dữ liệu được load qua observer
        
        // Thiết lập click listeners
        btnRegister.setOnClickListener(v -> onRegisterClick());
        btnCancel.setOnClickListener(v -> onCancelClick());
        btnJoinAuction.setOnClickListener(v -> onJoinAuctionClick());
        // initial header display
        updateHeader(sessionManager.getUserName(), sessionManager.getBalance());
    }
    
    private void setupObservers() {
        // Quan sát thông báo đăng ký
        viewModel.getRegistrationMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
        
        // Quan sát lỗi
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
        
        // Quan sát trạng thái loading
        viewModel.getIsRegistrationLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnRegister.setEnabled(!isLoading);
            btnCancel.setEnabled(!isLoading);
        });

        // Quan sát dữ liệu phiên đấu giá hiện tại
        viewModel.getCurrentItem().observe(this, item -> {
            if (item != null) {
                auctionItem = item;
                if (item.getProduct() != null) {
                    tvProductName.setText(item.getProduct().getProductName());
                }
                if (item.getAuction() != null) {
                    double startPrice = item.getAuction().getStartPrice() != null ? item.getAuction().getStartPrice() : 0;
                    tvStartPrice.setText(String.format("%,.0f VNĐ", startPrice));
                    tvRequiredDeposit.setText(String.format("%,.0f VNĐ", startPrice));
                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", new java.util.Locale("vi","VN"));
                    tvStartTime.setText(item.getAuction().getStartTime() != null ? df.format(item.getAuction().getStartTime()) : "--");
                    tvEndTime.setText(item.getAuction().getEndTime() != null ? df.format(item.getAuction().getEndTime()) : "--");
                }
                // show seller info if provided
                if (tvSellerName != null) tvSellerName.setText(item.getSellerUserName() != null ? item.getSellerUserName() : "--");
                if (tvSellerEmail != null) tvSellerEmail.setText(item.getSellerEmail() != null ? item.getSellerEmail() : "--");
                if (tvSellerPhone != null) tvSellerPhone.setText(item.getSellerPhone() != null ? item.getSellerPhone() : "--");

                // Toggle action buttons based on registration state
                if (item.isRegistered()) {
                    btnRegister.setVisibility(View.GONE);
                    btnJoinAuction.setVisibility(View.VISIBLE);
                    tvStatus.setText("Đã được duyệt");
                } else {
                    btnRegister.setVisibility(View.VISIBLE);
                    btnJoinAuction.setVisibility(View.GONE);
                    tvStatus.setText("Chưa đăng ký");
                }
            }
        });

        // Quan sát kết quả đăng ký để refresh số dư
        viewModel.getRegistrationSuccess().observe(this, success -> {
            if (success != null && success) {
                int userId = sessionManager.getUserId();
                if (userId != -1) {
                    userViewModel.getUserById(userId);
                }
                // Refresh auction item to update registration state and buttons
                viewModel.loadItem(auctionId, sessionManager.getUserId());
                Toast.makeText(this, "Đăng ký thành công! Bạn có thể vào phòng đấu giá.", Toast.LENGTH_SHORT).show();
                // Cập nhật ngay lập tức số dư hiển thị dựa trên tiền cọc (tránh cảm giác chậm)
                double deposit = 0;
                if (auctionItem != null && auctionItem.getAuction() != null && auctionItem.getAuction().getStartPrice() != null) {
                    deposit = auctionItem.getAuction().getStartPrice();
                }
                double cached = sessionManager.getBalance();
                double estimated = cached - deposit;
                if (deposit > 0) {
                    sessionManager.updateBalance(estimated);
                    java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi","VN"));
                    tvCurrentBalance.setText(formatter.format(estimated));
                    updateHeader(sessionManager.getUserName(), estimated);
                } else {
                    updateHeader(sessionManager.getUserName(), sessionManager.getBalance());
                }
                viewModel.resetRegistrationFlag();
            }
        });

        // Quan sát thông tin người dùng để hiển thị đầy đủ info và số dư
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                tvUserName.setText(user.getUserName() != null ? user.getUserName() : "-");
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "-");
                tvPhone.setText(user.getPhone() != null ? user.getPhone() : "-");
                double balance = user.getBalance() != null ? user.getBalance() : 0;
                java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi","VN"));
                tvCurrentBalance.setText(formatter.format(balance));
                // Cập nhật SessionManager để đồng bộ hiển thị ở nơi khác
                sessionManager.updateBalance(balance);
                updateHeader(user.getUserName(), balance);
            }
        });
    }
    
    private void loadUserData() {
        // Lấy thông tin người dùng hiện tại
        int userId = sessionManager.getUserId();
        if (userId != -1) {
            userViewModel.getUserById(userId);
        }
    }

    private void updateHeader(String userName, double balance) {
        if (tvHelloUserHeader != null) {
            tvHelloUserHeader.setText("Chào mừng, " + (userName != null ? userName : "Khách") + "!");
        }
        if (tvBalancePillHeader != null) {
            java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi","VN"));
            tvBalancePillHeader.setText(formatter.format(balance));
        }
    }

    @Override
    protected int getCurrentPageIndex() {
        return 2; // Auction tab
    }
    
    private void checkRegistrationStatus() {
        int userId = sessionManager.getUserId();
        if (userId != -1) {
            viewModel.checkEligibility(auctionId, userId);
        }
    }
    
    private void onRegisterClick() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để tham gia đấu giá", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Hiển thị dialog xác nhận
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận đăng ký")
            .setMessage("Bạn có chắc chắn muốn đăng ký tham gia phiên đấu giá này?")
            .setPositiveButton("Đăng ký", (dialog, which) -> {
                performRegistration(userId);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void performRegistration(int userId) {
        if (auctionItem == null || auctionItem.getAuction() == null) {
            Toast.makeText(this, "Đang tải dữ liệu phiên đấu giá...", Toast.LENGTH_SHORT).show();
            viewModel.loadItem(auctionId, userId);
            return;
        }
        double startPrice = auctionItem.getAuction().getStartPrice() != null ? auctionItem.getAuction().getStartPrice() : 0;
        viewModel.register(auctionId, userId, startPrice);
    }
    
    private void onCancelClick() {
        finish();
    }
    
    private void onJoinAuctionClick() {
        // Chuyển đến màn hình đấu giá
        Intent intent = new Intent(this, AuctionRoomActivity.class);
        intent.putExtra(AuctionRoomActivity.EXTRA_AUCTION_ID, auctionId);
        startActivity(intent);
    }
    
    private void updateUIForRegistrationStatus(AuctionRegistrations registration) {
        if (registration == null) {
            // Chưa đăng ký
            tvStatus.setText("Chưa đăng ký");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnRegister.setVisibility(View.VISIBLE);
            btnJoinAuction.setVisibility(View.GONE);
        } else {
            String status = registration.getStatus();
            switch (status) {
                case "pending":
                    tvStatus.setText("Đang chờ duyệt");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    btnRegister.setVisibility(View.GONE);
                    btnJoinAuction.setVisibility(View.GONE);
                    break;
                case "approved":
                    tvStatus.setText("Đã được duyệt");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    btnRegister.setVisibility(View.GONE);
                    btnJoinAuction.setVisibility(View.VISIBLE);
                    break;
                case "rejected":
                    tvStatus.setText("Đã bị từ chối");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    btnRegister.setVisibility(View.VISIBLE);
                    btnJoinAuction.setVisibility(View.GONE);
                    break;
                case "cancelled":
                    tvStatus.setText("Đã hủy");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    btnRegister.setVisibility(View.VISIBLE);
                    btnJoinAuction.setVisibility(View.GONE);
                    break;
                default:
                    tvStatus.setText("Trạng thái không xác định");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    btnRegister.setVisibility(View.VISIBLE);
                    btnJoinAuction.setVisibility(View.GONE);
                    break;
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup nếu cần
    }
}
