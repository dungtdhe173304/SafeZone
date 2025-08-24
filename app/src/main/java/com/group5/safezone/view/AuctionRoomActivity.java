package com.group5.safezone.view;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.adapter.BidHistoryAdapter;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.Bids;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.model.ui.AuctionItemUiModel;
import com.group5.safezone.repository.AuctionRepository;
import com.group5.safezone.viewmodel.AuctionViewModel;
import com.group5.safezone.viewmodel.UserViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionRoomActivity extends AppCompatActivity {

    public static final String EXTRA_AUCTION_ID = "auction_id";

    private int auctionId;
    private AuctionViewModel auctionViewModel;
    private UserViewModel userViewModel;
    private SessionManager sessionManager;
    private ExecutorService executorService;

    // UI Components
    private ImageView ivProductImage;
    private TextView tvProductName, tvSellerName, tvCurrentPrice, tvTimer;
    private RecyclerView rvBidHistory;
    private EditText etBidStep;
    private TextView tvQuantity, tvTotalBid;
    private Button btnMinus, btnPlus, btnPlaceBid;

    // Data
    private AuctionItemUiModel currentItem;
    private User currentUser;
    private double currentHighestBid;
    private double startPrice;
    private double bidStep;
    private int bidQuantity = 1;
    private CountDownTimer countDownTimer;
    private long timeRemaining;
    private BidHistoryAdapter bidHistoryAdapter;

    // Formatters
    private NumberFormat currencyFormatter;
    private SimpleDateFormat timeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction_room);

        try {
            // Initialize
            auctionId = getIntent().getIntExtra(EXTRA_AUCTION_ID, -1);
            if (auctionId == -1) {
                Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            initViews();
            initViewModels();
            setupFormatters();
            setupRecyclerView();
            setupClickListeners();
            loadAuctionData();

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo phòng đấu giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvCurrentPrice = findViewById(R.id.tv_current_price);
        tvTimer = findViewById(R.id.tv_timer);
        rvBidHistory = findViewById(R.id.rv_bid_history);
        etBidStep = findViewById(R.id.et_bid_step);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvTotalBid = findViewById(R.id.tv_total_bid);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);
        btnPlaceBid = findViewById(R.id.btn_place_bid);
    }

    private void initViewModels() {
        auctionViewModel = new ViewModelProvider(this).get(AuctionViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        sessionManager = new SessionManager(this);
        executorService = Executors.newFixedThreadPool(4);
        
        // Debug session after initialization
        int userId = sessionManager.getUserId();
        String userName = sessionManager.getUserName();
        android.util.Log.d("AuctionRoom", "After initViewModels - UserID: " + userId + ", UserName: " + userName);
    }

    private void setupFormatters() {
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        timeFormatter = new SimpleDateFormat("mm:ss", new Locale("vi", "VN"));
    }

    private void setupRecyclerView() {
        bidHistoryAdapter = new BidHistoryAdapter(this);
        rvBidHistory.setLayoutManager(new LinearLayoutManager(this));
        rvBidHistory.setAdapter(bidHistoryAdapter);
    }

    private void setupClickListeners() {
        btnMinus.setOnClickListener(v -> {
            if (bidQuantity > 1) {
                bidQuantity--;
                updateBidQuantity();
            }
        });

        btnPlus.setOnClickListener(v -> {
            bidQuantity++;
            updateBidQuantity();
        });

        etBidStep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!s.toString().isEmpty()) {
                        bidStep = Double.parseDouble(s.toString());
                        updateBidQuantity();
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        });

        btnPlaceBid.setOnClickListener(v -> placeBid());
    }

    private void loadAuctionData() {
        try {
            // Load auction data
            auctionViewModel.loadItem(auctionId, sessionManager.getUserId());
            auctionViewModel.getCurrentItem().observe(this, item -> {
                try {
                    if (item != null) {
                        currentItem = item;
                        updateUI();
                        loadBidHistory();
                        startTimer();
                    } else {
                        Toast.makeText(AuctionRoomActivity.this, "Không tìm thấy thông tin phiên đấu giá", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (Exception e) {
                    Toast.makeText(AuctionRoomActivity.this, "Lỗi khi tải dữ liệu phiên đấu giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            // Load user data
            int userId = sessionManager.getUserId();
            if (userId > 0) {
                userViewModel.getUserById(userId);
                userViewModel.getCurrentUser().observe(this, user -> {
                    try {
                                            if (user != null) {
                        currentUser = user;
                        android.util.Log.d("AuctionRoom", "User loaded via ViewModel: " + user.getUserName() + ", Balance: " + user.getBalance());
                    } else {
                        android.util.Log.e("AuctionRoom", "User is null after getUserById, trying direct load");
                        // Fallback: try to load user directly from database
                        loadUserDirectly(userId);
                    }
                    } catch (Exception e) {
                        android.util.Log.e("AuctionRoom", "Error loading user via ViewModel: " + e.getMessage());
                        // Fallback: try to load user directly from database
                        loadUserDirectly(userId);
                    }
                });
            } else {
                android.util.Log.e("AuctionRoom", "No user ID in session");
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi khởi tạo dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateUI() {
        try {
            if (currentItem != null) {
                Product product = currentItem.getProduct();
                Auctions auction = currentItem.getAuction();

                if (product != null) {
                    tvProductName.setText(product.getProductName() != null ? product.getProductName() : "Sản phẩm");
                }

                if (auction != null) {
                    // Sử dụng startPrice làm giá khởi điểm
                    startPrice = auction.getStartPrice() != null ? auction.getStartPrice() : 0.0;
                    
                    // Nếu chưa có ai trả giá, sử dụng startPrice
                    if (auction.getCurrentHighestBid() == null || auction.getCurrentHighestBid() < startPrice) {
                        currentHighestBid = startPrice;
                    } else {
                        currentHighestBid = auction.getCurrentHighestBid();
                    }
                    
                    bidStep = product != null && product.getMinBidIncrement() != null ? product.getMinBidIncrement() : 100000.0;
                    
                    tvCurrentPrice.setText("Giá hiện tại: " + currencyFormatter.format(currentHighestBid));
                    tvSellerName.setText("Người bán: " + (currentItem.getSellerUserName() != null ? currentItem.getSellerUserName() : "--"));
                    
                    // Set bid step in EditText
                    etBidStep.setText(String.format("%.0f", bidStep));
                }
            }
            
            updateBidQuantity();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi cập nhật giao diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBidQuantity() {
        try {
            tvQuantity.setText(String.valueOf(bidQuantity));
            double totalBid = bidStep * bidQuantity;
            tvTotalBid.setText(String.format("%.0f", totalBid));
            
            // Update place bid button text
            double newBidAmount = currentHighestBid + totalBid;
            btnPlaceBid.setText("Trả giá " + currencyFormatter.format(newBidAmount));
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi cập nhật số lượng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBidHistory() {
        try {
            executorService.execute(() -> {
                try {
                    AuctionRepository repository = auctionViewModel.getRepository();
                    List<Bids> bids = repository.getDatabase().bidsDao().getBidsByAuctionId(auctionId);
                    // Build userId -> userName map for display
                    Map<Integer, String> nameMap = new HashMap<>();
                    if (bids != null) {
                        for (Bids b : bids) {
                            if (b != null) {
                                Integer bidderId = b.getBidderUserId();
                                if (bidderId != null && !nameMap.containsKey(bidderId)) {
                                    try {
                                        User u = repository.getDatabase().userDao().getUserById(bidderId);
                                        nameMap.put(bidderId, (u != null && u.getUserName() != null) ? u.getUserName() : null);
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                    
                    runOnUiThread(() -> {
                        try {
                            bidHistoryAdapter.setUserIdToNameMap(nameMap);
                            bidHistoryAdapter.submitList(bids);
                        } catch (Exception e) {
                            Toast.makeText(AuctionRoomActivity.this, "Lỗi khi hiển thị lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AuctionRoomActivity.this, "Lỗi khi tải lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi khởi tạo lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void placeBid() {
        try {
            // Check session first
            int userId = sessionManager.getUserId();
            boolean isLoggedIn = sessionManager.isLoggedIn();
            String userName = sessionManager.getUserName();
            
            android.util.Log.d("AuctionRoom", "placeBid - UserID: " + userId + ", isLoggedIn: " + isLoggedIn + ", UserName: " + userName);
            
            if (userId <= 0 || !isLoggedIn) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            // If currentUser is null, try to get from session
            if (currentUser == null) {
                // Try to load user from database
                executorService.execute(() -> {
                    try {
                        AuctionRepository repository = auctionViewModel.getRepository();
                        User user = repository.getDatabase().userDao().getUserById(userId);
                        runOnUiThread(() -> {
                            if (user != null) {
                                currentUser = user;
                                // Retry placing bid
                                placeBid();
                            } else {
                                Toast.makeText(AuctionRoomActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(AuctionRoomActivity.this, "Lỗi khi tải thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
                return;
            }

            if (currentItem == null || currentItem.getAuction() == null) {
                Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            double bidAmount = currentHighestBid + (bidStep * bidQuantity);
            
            if (bidAmount <= currentHighestBid) {
                Toast.makeText(this, "Giá trả phải cao hơn giá hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent multiple clicks
            btnPlaceBid.setEnabled(false);

            executorService.execute(() -> {
                try {
                    AuctionRepository repository = auctionViewModel.getRepository();
                    
                    // Create new bid
                    Bids newBid = new Bids();
                    newBid.setAuctionId(auctionId);
                    newBid.setBidderUserId(currentUser.getId());
                    newBid.setBidAmount(bidAmount);
                    newBid.setBidTime(new Date());
                    newBid.setStatus("active");
                    
                    // Insert bid
                    repository.getDatabase().bidsDao().insert(newBid);
                    
                    // Update auction
                    Auctions auction = currentItem.getAuction();
                    auction.setCurrentHighestBid(bidAmount);
                    auction.setHighestBidderUserId(currentUser.getId());
                    repository.getDatabase().auctionsDao().update(auction);
                    
                    // Không trừ balance khi trả giá - người dùng được phép trả giá tự do
                    
                    runOnUiThread(() -> {
                        try {
                            // Update UI
                            currentHighestBid = bidAmount;
                            updateUI();
                            loadBidHistory();
                            
                            // Show success message
                            Toast.makeText(AuctionRoomActivity.this, "Trả giá thành công!", Toast.LENGTH_SHORT).show();
                            
                            // Re-enable button
                            btnPlaceBid.setEnabled(true);
                        } catch (Exception e) {
                            Toast.makeText(AuctionRoomActivity.this, "Lỗi khi cập nhật giao diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            btnPlaceBid.setEnabled(true);
                        }
                    });
                    
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AuctionRoomActivity.this, "Lỗi khi trả giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnPlaceBid.setEnabled(true);
                    });
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi xử lý trả giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnPlaceBid.setEnabled(true);
        }
    }

    private void startTimer() {
        try {
            if (currentItem != null && currentItem.getAuction() != null && currentItem.getAuction().getEndTime() != null) {
                timeRemaining = currentItem.getAuction().getEndTime().getTime() - System.currentTimeMillis();
                
                if (timeRemaining > 0) {
                    countDownTimer = new CountDownTimer(timeRemaining, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            timeRemaining = millisUntilFinished;
                            updateTimerDisplay();
                        }

                        @Override
                        public void onFinish() {
                            tvTimer.setText("00:00");
                            btnPlaceBid.setEnabled(false);
                            Toast.makeText(AuctionRoomActivity.this, "Phiên đấu giá đã kết thúc!", Toast.LENGTH_LONG).show();
                        }
                    }.start();
                } else {
                    tvTimer.setText("00:00");
                    btnPlaceBid.setEnabled(false);
                }
            } else {
                tvTimer.setText("--:--");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi khởi tạo timer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            tvTimer.setText("--:--");
        }
    }

    private void updateTimerDisplay() {
        try {
            long minutes = timeRemaining / 60000;
            long seconds = (timeRemaining % 60000) / 1000;
            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
        } catch (Exception e) {
            tvTimer.setText("--:--");
        }
    }

    // Bỏ method updateUserBalanceDisplay vì không còn hiển thị số dư

    private void loadUserDirectly(int userId) {
        executorService.execute(() -> {
            try {
                AuctionRepository repository = auctionViewModel.getRepository();
                User user = repository.getDatabase().userDao().getUserById(userId);
                runOnUiThread(() -> {
                    if (user != null) {
                        currentUser = user;
                        android.util.Log.d("AuctionRoom", "User loaded directly: " + user.getUserName() + ", Balance: " + user.getBalance());
                    } else {
                        android.util.Log.e("AuctionRoom", "User not found in database for ID: " + userId);
                        // Try to create user from session data
                        createUserFromSession();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    android.util.Log.e("AuctionRoom", "Error loading user directly: " + e.getMessage());
                    // Try to create user from session data
                    createUserFromSession();
                });
            }
        });
    }

    private void createUserFromSession() {
        try {
            int userId = sessionManager.getUserId();
            String userName = sessionManager.getUserName();
            String email = sessionManager.getEmail();
            String role = sessionManager.getUserRole();
            String status = sessionManager.getUserStatus();
            double balance = sessionManager.getBalance();
            boolean isVerify = sessionManager.isVerified();

            if (userId > 0 && userName != null) {
                User user = new User();
                user.setId(userId);
                user.setUserName(userName);
                user.setEmail(email);
                user.setRole(role);
                user.setStatus(status);
                user.setBalance(balance);
                user.setIsVerify(isVerify);

                currentUser = user;
                android.util.Log.d("AuctionRoom", "User created from session: " + user.getUserName() + ", Balance: " + user.getBalance());
            } else {
                android.util.Log.e("AuctionRoom", "Cannot create user from session - insufficient data");
                Toast.makeText(this, "Dữ liệu phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("AuctionRoom", "Error creating user from session: " + e.getMessage());
            Toast.makeText(this, "Lỗi khi tạo thông tin người dùng từ phiên đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
