package com.group5.safezone.view.Wallet;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.group5.safezone.Constant.Api.CreateOrder;
import com.group5.safezone.R;
import com.group5.safezone.adapter.TransactionAdapter;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Transactions;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.repository.TransactionRepository;
import com.group5.safezone.repository.UserRepository;
import com.group5.safezone.repository.NotificationRepository;
import com.group5.safezone.model.entity.Notification;
import com.group5.safezone.config.AppNotificationHelper;

import org.json.JSONObject;
import org.json.JSONException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class WalletActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvBalance, tvUserName;
    private MaterialButton btnDeposit, btnWithdraw;
    private TextView tvViewAll;
    private RecyclerView rvTransactions;
    private View emptyState;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private NotificationRepository notificationRepository;
    private TransactionAdapter transactionAdapter;
    private List<Transactions> transactionList;
    private long currentTransactionId = -1; // Lưu ID của transaction hiện tại
    private double currentDepositAmount = 0; // Lưu số tiền nạp hiện tại
    private static final int REQ_POST_NOTIF = 5010;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("WalletActivity", "=== onCreate called ===");
        
        try {
            setContentView(R.layout.activity_wallet);

            // Initialize StrictMode for network operations
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Initialize ZaloPay SDK
            ZaloPaySDK.init(2553, Environment.SANDBOX);

            initViews();
            initData();
            setupToolbar();
            setupRecyclerView();
            loadUserData();
            loadTransactions();
            setupClickListeners();
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("WalletActivity", "Error in onCreate", e);
            // Hiển thị lỗi cho user
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            tvBalance = findViewById(R.id.tvBalance);
            tvUserName = findViewById(R.id.tvUserName);
            btnDeposit = findViewById(R.id.btnDeposit);
            btnWithdraw = findViewById(R.id.btnWithdraw);
            tvViewAll = findViewById(R.id.tvViewAll);
            rvTransactions = findViewById(R.id.rvTransactions);
            emptyState = findViewById(R.id.emptyState);
            progressBar = findViewById(R.id.progressBar);
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("WalletActivity", "Error in initViews", e);
            throw e;
        }
    }

    private void initData() {
        sessionManager = new SessionManager(this);
        userRepository = new UserRepository(getApplication());
        transactionRepository = new TransactionRepository(getApplication());
        notificationRepository = new NotificationRepository(getApplication());
        transactionList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(this, transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);

        transactionAdapter.setOnTransactionClickListener(transaction -> {
            // Handle transaction click if needed
            Toast.makeText(this, getString(R.string.transaction_history) + ": " + transaction.getDescription(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        
        android.util.Log.d("WalletActivity", "Loading user data from database for userId: " + userId);
        
        // Lấy balance từ database để đảm bảo accuracy và real-time
        // Sử dụng background thread để tránh lỗi main thread
        new Thread(() -> {
            try {
                User user = userRepository.getUserById(userId);
                
                if (user != null) {
                    double balance = user.getBalance() != null ? user.getBalance() : 0.0;
                    
                    android.util.Log.d("WalletActivity", "Database balance: " + balance);
                    
                    // Cập nhật UI trên main thread
                    runOnUiThread(() -> {
                        // Cập nhật balance trong session để đồng bộ
                        sessionManager.updateBalance(balance);
                        
                        // Hiển thị balance
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                        String formattedBalance = formatter.format(balance);
                        tvBalance.setText(formattedBalance);
                        
                        // Hiển thị username từ database
                        tvUserName.setText(user.getUserName() != null ? user.getUserName() : "Người dùng");
                        
                        android.util.Log.d("WalletActivity", "User data loaded successfully from database");
                    });
                } else {
                    android.util.Log.e("WalletActivity", "User not found in database for userId: " + userId);
                    runOnUiThread(() -> {
                        Toast.makeText(WalletActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("WalletActivity", "Error loading user data from database: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(WalletActivity.this, "Lỗi tải thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadTransactions() {
        showLoading(true);
        int userId = sessionManager.getUserId();
        
        try {
            transactionRepository.getRecentTransactions(userId, 10, new TransactionRepository.OnTransactionCallback() {
                @Override
                public void onResult(List<Transactions> transactions) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        if (transactions != null && !transactions.isEmpty()) {
                            transactionList.clear();
                            transactionList.addAll(transactions);
                            transactionAdapter.notifyDataSetChanged();
                            rvTransactions.setVisibility(View.VISIBLE);
                            emptyState.setVisibility(View.GONE);
                        } else {
                            rvTransactions.setVisibility(View.GONE);
                            emptyState.setVisibility(View.GONE);
                        }
                        
                        // Refresh balance sau khi load transactions
                        refreshBalance();
                    });
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                showLoading(false);
                rvTransactions.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                Toast.makeText(WalletActivity.this, "Lỗi tải lịch sử giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("WalletActivity", "Error loading transactions", e);
            });
        }
    }

    private void setupClickListeners() {
        btnDeposit.setOnClickListener(v -> showDepositDialog());
        btnWithdraw.setOnClickListener(v -> showWithdrawDialog());
        tvViewAll.setOnClickListener(v -> {
            // TODO: Navigate to full transaction history
            Toast.makeText(this, getString(R.string.view_all), Toast.LENGTH_SHORT).show();
        });
    }

    // Method để refresh balance sau khi có giao dịch mới
    public void refreshBalance() {
        android.util.Log.d("WalletActivity", "Refreshing balance from database...");
        loadUserData();
    }
    
    // Method để refresh balance và transactions real-time
    public void refreshAllData() {
        android.util.Log.d("WalletActivity", "Refreshing all data from database...");
        loadUserData();
        loadTransactions();
    }
    
    // Method để refresh balance sau khi deposit thành công
    public void onDepositSuccess(double newBalance) {
        android.util.Log.d("WalletActivity", "Deposit successful, new balance: " + newBalance);
        
        // Cập nhật balance trong session
        sessionManager.updateBalance(newBalance);
        
        // Hiển thị balance mới
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedBalance = formatter.format(newBalance);
        tvBalance.setText(formattedBalance);
        
        // Refresh transactions để hiển thị giao dịch mới
        loadTransactions();
        
        Toast.makeText(this, "Nạp tiền thành công! Số dư mới: " + formattedBalance, Toast.LENGTH_LONG).show();
    }





    private void showDepositDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_deposit, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Initialize dialog views
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Chip chip50k = dialogView.findViewById(R.id.chip50k);
        Chip chip100k = dialogView.findViewById(R.id.chip100k);
        Chip chip200k = dialogView.findViewById(R.id.chip200k);
        Chip chip500k = dialogView.findViewById(R.id.chip500k);
        Chip chip1M = dialogView.findViewById(R.id.chip1M);
        Chip chip5M = dialogView.findViewById(R.id.chip5M);
        Chip chipSug1 = dialogView.findViewById(R.id.chipSug1);
        Chip chipSug2 = dialogView.findViewById(R.id.chipSug2);
        Chip chipSug3 = dialogView.findViewById(R.id.chipSug3);
        TextView tvSmartSuggest = dialogView.findViewById(R.id.tvSmartSuggest);
        ChipGroup smartGroup = dialogView.findViewById(R.id.layoutSmartSuggest);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        // Setup chip listeners
        chip50k.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etAmount.setText("50000");
                uncheckOtherChips(chip50k, chip100k, chip200k, chip500k, chip1M, chip5M);
            }
        });

        chip100k.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etAmount.setText("100000");
                uncheckOtherChips(chip100k, chip50k, chip200k, chip500k, chip1M, chip5M);
            }
        });

        chip200k.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etAmount.setText("200000");
                uncheckOtherChips(chip200k, chip50k, chip100k, chip500k, chip1M, chip5M);
            }
        });

        chip500k.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etAmount.setText("500000");
                uncheckOtherChips(chip500k, chip50k, chip100k, chip200k, chip1M, chip5M);
            }
        });

        chip1M.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etAmount.setText("1000000");
                uncheckOtherChips(chip1M, chip50k, chip100k, chip200k, chip500k, chip5M);
            }
        });

        chip5M.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etAmount.setText("5000000");
                uncheckOtherChips(chip5M, chip50k, chip100k, chip200k, chip500k, chip1M);
            }
        });

        // Format input as 1.000 style and update smart suggestions
        final boolean[] isFormatting = {false};
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isFormatting[0]) return;
                isFormatting[0] = true;
                String raw = s.toString().replaceAll("[^0-9]", "");
                if (!raw.isEmpty()) {
                    try {
                        long val = Long.parseLong(raw);
                        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi","VN"));
                        String formatted = nf.format(val);
                        etAmount.setText(formatted);
                        etAmount.setSelection(formatted.length());
                    } catch (NumberFormatException ignore) {}
                }
                boolean hasNumber = !raw.isEmpty();
                tvSmartSuggest.setVisibility(hasNumber ? View.VISIBLE : View.GONE);
                smartGroup.setVisibility(hasNumber ? View.VISIBLE : View.GONE);
                updateSuggestionChips(etAmount.getText().toString(), chipSug1, chipSug2, chipSug3);
                isFormatting[0] = false;
            }
        };
        etAmount.addTextChangedListener(watcher);

        View.OnClickListener sugClick = v -> {
            Chip c = (Chip) v;
            String numeric = c.getText().toString().replace(".", "");
            etAmount.setText(numeric);
        };
        chipSug1.setOnClickListener(sugClick);
        chipSug2.setOnClickListener(sugClick);
        chipSug3.setOnClickListener(sugClick);

        // Setup button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String amountText = etAmount.getText().toString().trim();
            if (amountText.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_amount), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String digitsOnly = amountText.replaceAll("[^0-9]", "");
                double amount = Double.parseDouble(digitsOnly);
                if (amount < 10000) {
                    Toast.makeText(this, "Số tiền nạp tối thiểu là 10.000 VNĐ", Toast.LENGTH_SHORT).show();
                    return;
                }



                dialog.dismiss();
                processDeposit(amount);
            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateSuggestionChips(String input, Chip chip1, Chip chip2, Chip chip3) {
        try {
            String digits = input.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) {
                setSuggestionText(chip1, 10000);
                setSuggestionText(chip2, 50000);
                setSuggestionText(chip3, 100000);
                return;
            }

            // Lấy toàn bộ số người dùng nhập làm hệ số cho nghìn
            long factor = Long.parseLong(digits);
            if (factor <= 0) factor = 1;

            // Ví dụ nhập 65 -> 65k, 650k, 6.5M (làm tròn xuống nghìn)
            long s1 = Math.max(10_000L, factor * 1_000L); // tối thiểu 10k
            long s2 = factor * 10_000L;       // x * 10 nghìn
            long s3 = factor * 100_000L;      // x * 100 nghìn

            setSuggestionText(chip1, s1);
            setSuggestionText(chip2, s2);
            setSuggestionText(chip3, s3);
        } catch (Exception e) {
            setSuggestionText(chip1, 10000);
            setSuggestionText(chip2, 50000);
            setSuggestionText(chip3, 100000);
        }
    }

    private void setSuggestionText(Chip chip, long amount) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        chip.setText(nf.format(amount));
    }

    private void uncheckOtherChips(Chip selectedChip, Chip... otherChips) {
        for (Chip chip : otherChips) {
            if (chip != selectedChip) {
                chip.setChecked(false);
            }
        }
    }

    private void showWithdrawDialog() {
        // TODO: Implement withdraw functionality
        Toast.makeText(this, getString(R.string.withdraw_development), Toast.LENGTH_SHORT).show();
    }

    private boolean shouldShowNotification() {
        if (android.os.Build.VERSION.SDK_INT < 33) return true;
        int granted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS);
        return granted == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPermissionWithRationale() {
        if (android.os.Build.VERSION.SDK_INT < 33) return;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.notif_permission_title))
                    .setMessage(getString(R.string.notif_permission_msg))
                    .setPositiveButton(getString(R.string.allow), (d, w) -> ActivityCompat.requestPermissions(
                            this,
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            REQ_POST_NOTIF))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQ_POST_NOTIF);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIF && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Quyền đã cấp, có thể hiển thị thông báo cho lần tới; không hồi tố số tiền ở đây
            Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();
        }
    }

    private void processDeposit(double amount) {
        // Double check validation
        if (amount < 10000) {
            Toast.makeText(this, "Số tiền nạp tối thiểu là 10.000 VNĐ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Lưu số tiền hiện tại để có thể retry
        currentDepositAmount = amount;
        showLoading(true);
        
        try {
            // Create ZaloPay order trước
            CreateOrder orderApi = new CreateOrder();
            String amountString = String.valueOf((int) amount);
            android.util.Log.d("WalletActivity", "Creating ZaloPay order with amount: " + amountString);
            JSONObject data = orderApi.createOrder(amountString);
            android.util.Log.d("WalletActivity", "ZaloPay API response: " + (data != null ? data.toString() : "NULL"));
            
            // Kiểm tra response có hợp lệ không
            if (!isValidZaloPayResponse(data)) {
                showLoading(false);
                Toast.makeText(this, "Lỗi: Response từ ZaloPay không hợp lệ", Toast.LENGTH_LONG).show();
                return;
            }
            
            String code = data.getString("return_code");
            if ("1".equals(code)) {
                // Kiểm tra zp_trans_token có tồn tại không
                if (!data.has("zp_trans_token")) {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi: Response không có zp_trans_token", Toast.LENGTH_LONG).show();
                    android.util.Log.e("WalletActivity", "ZaloPay API response missing zp_trans_token: " + data.toString());
                    return;
                }
                
                String token = data.getString("zp_trans_token");
                
                // Tạo transaction với status PENDING
                Transactions pendingTransaction = new Transactions();
                pendingTransaction.setUserId(sessionManager.getUserId());
                pendingTransaction.setTransactionType(Transactions.TYPE_DEPOSIT);
                pendingTransaction.setAmount(amount);
                pendingTransaction.setDescription(getString(R.string.deposit) + " qua ZaloPay");
                pendingTransaction.setStatus(Transactions.STATUS_PENDING);
                pendingTransaction.setReferenceId(token);
                
                // Insert transaction vào database
                android.util.Log.d("WalletActivity", "Inserting transaction: " + pendingTransaction.getAmount());
                transactionRepository.insertTransaction(pendingTransaction, new TransactionRepository.OnInsertCallback() {
                    @Override
                    public void onResult(long transactionId) {
                        currentTransactionId = transactionId; // Lưu ID để update sau
                        android.util.Log.d("WalletActivity", "Transaction inserted with ID: " + transactionId);
                        
                        // Process ZaloPay payment
                        android.util.Log.d("WalletActivity", "Starting ZaloPay payment with token: " + token);
                        android.util.Log.d("WalletActivity", "ZaloPay SDK instance: " + ZaloPaySDK.getInstance());
                        android.util.Log.d("WalletActivity", "Starting ZaloPay payment with callback URL: safezone://");
                        android.util.Log.d("WalletActivity", "About to call ZaloPaySDK.payOrder...");
                        try {
                            ZaloPaySDK.getInstance().payOrder(WalletActivity.this, token, "safezone://", new PayOrderListener() {
                                @Override
                                public void onPaymentSucceeded(String transactionId, String zpTransToken, String appTransId) {
                                    android.util.Log.d("WalletActivity", "=== ZALOPAY CALLBACK: onPaymentSucceeded ===");
                                    android.util.Log.d("WalletActivity", "transactionId: " + transactionId);
                                    android.util.Log.d("WalletActivity", "zpTransToken: " + zpTransToken);
                                    android.util.Log.d("WalletActivity", "appTransId: " + appTransId);
                                    android.util.Log.d("WalletActivity", "ZaloPay payment succeeded: " + transactionId);
                                    
                                    // Update transaction status to SUCCESS
                                    android.util.Log.d("WalletActivity", "Updating transaction to SUCCESS: " + transactionId);
                                    // Update trong database bằng ID
                                    transactionRepository.updateTransactionStatus(currentTransactionId, Transactions.STATUS_SUCCESS, transactionId);
                                    
                                    // Update user balance
                                    android.util.Log.d("WalletActivity", "Updating user balance with amount: " + amount);
                                    updateUserBalance(amount);
                                    
                                                            // Refresh UI
                                                            runOnUiThread(() -> {
                                        showLoading(false);
                                        currentDepositAmount = 0; // Reset số tiền nạp
                                        android.util.Log.d("WalletActivity", "Payment UI refresh completed");
                            
                            // Balance và transactions sẽ được refresh trong onDepositSuccess
                            // Không cần gọi refreshBalance() và loadTransactions() ở đây nữa
                                        // Tạo thông báo nạp tiền thành công (không đổi schema DB)
                                        try {
                                            Notification n = new Notification();
                                            n.setUserId(sessionManager.getUserId());
                                            n.setType("DEPOSIT_SUCCESS");
                                            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
                                            n.setMessage("Nạp tiền thành công " + nf.format(amount));
                                            n.setRelatedEntityType("TRANSACTION");
                                            n.setRelatedEntityId((int) currentTransactionId);
                                            n.setCreatedBy(sessionManager.getUserId());
                                            notificationRepository.insert(n);
                                        } catch (Exception e) { /* ignore */ }

                                        // Popup notification ngay trên hệ thống
                                        if (shouldShowNotification()) {
                                            AppNotificationHelper.showDepositSuccess(WalletActivity.this, amount, (int) currentTransactionId);
                                        } else {
                                            requestNotificationPermissionWithRationale();
                                        }
                                    });
                                }

                                @Override
                                public void onPaymentCanceled(String zpTransToken, String appTransId) {
                                    android.util.Log.d("WalletActivity", "=== ZALOPAY CALLBACK: onPaymentCanceled ===");
                                    android.util.Log.d("WalletActivity", "zpTransToken: " + zpTransToken);
                                    android.util.Log.d("WalletActivity", "appTransId: " + appTransId);
                                    android.util.Log.d("WalletActivity", "ZaloPay payment canceled: " + zpTransToken);
                                    
                                    // Update transaction status to FAILED
                                    transactionRepository.updateTransactionStatus(currentTransactionId, Transactions.STATUS_FAILED, zpTransToken);
                                    
                                    runOnUiThread(() -> {
                                        showLoading(false);
                                        Toast.makeText(WalletActivity.this, "Hủy nạp tiền", Toast.LENGTH_LONG).show();
                                        loadTransactions();
                                    });
                                }

                                @Override
                                public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransId) {
                                    android.util.Log.d("WalletActivity", "=== ZALOPAY CALLBACK: onPaymentError ===");
                                    android.util.Log.d("WalletActivity", "zaloPayError: " + zaloPayError.toString());
                                    android.util.Log.d("WalletActivity", "zpTransToken: " + zpTransToken);
                                    android.util.Log.d("WalletActivity", "appTransId: " + appTransId);
                                    android.util.Log.d("WalletActivity", "ZaloPay payment error: " + zaloPayError.toString());
                                    
                                    // Update transaction status to FAILED
                                    transactionRepository.updateTransactionStatus(currentTransactionId, Transactions.STATUS_FAILED, zpTransToken);
                                    
                                    runOnUiThread(() -> {
                                        showLoading(false);
                                        Toast.makeText(WalletActivity.this, "Lỗi nạp tiền: " + zaloPayError.toString(), Toast.LENGTH_LONG).show();
                                        loadTransactions();
                                    });
                                }
                            });
                            android.util.Log.d("WalletActivity", "ZaloPaySDK.payOrder called successfully");
                        } catch (Exception e) {
                            android.util.Log.e("WalletActivity", "Error calling ZaloPaySDK.payOrder", e);
                            showLoading(false);
                            Toast.makeText(WalletActivity.this, "Lỗi khởi tạo ZaloPay: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                showLoading(false);
                handleZaloPayError(data, "tạo đơn hàng");
            }
        } catch (Exception e) {
            showLoading(false);
            String errorMessage = "Lỗi: " + e.getMessage();
            if (e instanceof JSONException) {
                errorMessage = "Lỗi xử lý dữ liệu từ ZaloPay: " + e.getMessage();
            } else if (e instanceof IllegalArgumentException) {
                errorMessage = "Lỗi tham số: " + e.getMessage();
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            android.util.Log.e("WalletActivity", "Error in processDeposit", e);
        }
    }

    private void updateUserBalance(double amount) {
        int userId = sessionManager.getUserId();
        android.util.Log.d("WalletActivity", "Starting balance update for user " + userId + " with amount " + amount);
        
        // Sử dụng background thread để tránh lỗi main thread
        new Thread(() -> {
            try {
                // Update trong database trước
                android.util.Log.d("WalletActivity", "Calling userRepository.updateBalance...");
                userRepository.updateBalance(userId, amount);
                android.util.Log.d("WalletActivity", "userRepository.updateBalance completed");
                
                // Lấy balance mới từ database để đảm bảo accuracy
                User updatedUser = userRepository.getUserById(userId);
                if (updatedUser != null) {
                    double newBalance = updatedUser.getBalance() != null ? updatedUser.getBalance() : 0.0;
                    android.util.Log.d("WalletActivity", "New balance from database: " + newBalance);
                    
                    // Update session balance để đồng bộ
                    sessionManager.updateBalance(newBalance);
                    
                    // Gọi callback để cập nhật UI real-time trên main thread
                    runOnUiThread(() -> onDepositSuccess(newBalance));
                    
                    android.util.Log.d("WalletActivity", "Balance update completed successfully");
                } else {
                    android.util.Log.e("WalletActivity", "Failed to get updated user data from database");
                    runOnUiThread(() -> {
                        Toast.makeText(WalletActivity.this, "Lỗi cập nhật số dư: Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    });
                }
                
            } catch (Exception e) {
                android.util.Log.e("WalletActivity", "Error updating balance", e);
                runOnUiThread(() -> {
                    Toast.makeText(WalletActivity.this, "Lỗi cập nhật số dư: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    /**
     * Kiểm tra response từ ZaloPay API có hợp lệ không
     */
    private boolean isValidZaloPayResponse(JSONObject response) {
        if (response == null) {
            android.util.Log.e("WalletActivity", "ZaloPay response is null");
            return false;
        }
        
        if (!response.has("return_code")) {
            android.util.Log.e("WalletActivity", "ZaloPay response missing return_code: " + response.toString());
            return false;
        }
        
        if (!response.has("return_message")) {
            android.util.Log.e("WalletActivity", "ZaloPay response missing return_message: " + response.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * Xử lý lỗi từ ZaloPay API
     */
    private void handleZaloPayError(JSONObject response, String operation) {
        try {
            String errorCode = response.optString("return_code", "UNKNOWN");
            String errorMessage = response.optString("return_message", "Lỗi không xác định");
            
            android.util.Log.e("WalletActivity", "ZaloPay " + operation + " error - Code: " + errorCode + ", Message: " + errorMessage);
            android.util.Log.e("WalletActivity", "Full response: " + response.toString());
            
            String userMessage = "Lỗi ZaloPay (" + errorCode + "): " + errorMessage;
            
            // Hiển thị dialog với tùy chọn retry
            new AlertDialog.Builder(this)
                .setTitle("Lỗi ZaloPay")
                .setMessage(userMessage + "\n\nBạn có muốn thử lại không?")
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    // Retry với số tiền hiện tại
                    if (currentDepositAmount > 0) {
                        processDeposit(currentDepositAmount);
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    showLoading(false);
                })
                .setCancelable(false)
                .show();
            
        } catch (Exception e) {
            android.util.Log.e("WalletActivity", "Error handling ZaloPay error response", e);
            Toast.makeText(this, "Lỗi xử lý phản hồi từ ZaloPay", Toast.LENGTH_LONG).show();
            showLoading(false);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("WalletActivity", "=== onResume called ===");
        android.util.Log.d("WalletActivity", "Activity state: " + isFinishing() + ", isDestroyed: " + isDestroyed());
        
        // Refresh balance và transactions khi quay lại Wallet
        // Điều này sẽ cập nhật balance sau khi đăng bài hoặc có giao dịch mới
        android.util.Log.d("WalletActivity", "Refreshing data on resume from database...");
        refreshAllData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        android.util.Log.d("WalletActivity", "=== onNewIntent called ===");
        android.util.Log.d("WalletActivity", "Intent action: " + intent.getAction());
        android.util.Log.d("WalletActivity", "Intent data: " + intent.getData());
        android.util.Log.d("WalletActivity", "Intent extras: " + intent.getExtras());
        
        try {
            ZaloPaySDK.getInstance().onResult(intent);
            android.util.Log.d("WalletActivity", "ZaloPaySDK.onResult called successfully");
        } catch (Exception e) {
            android.util.Log.e("WalletActivity", "Error calling ZaloPaySDK.onResult", e);
        }

        // Nếu mở từ notification, có thể cuộn/nhấn highlight hoặc load lại
        if (intent != null && intent.getBooleanExtra("open_from_notification", false)) {
            // Refresh dữ liệu để người dùng thấy số dư và giao dịch mới nhất
            android.util.Log.d("WalletActivity", "Opening from notification, refreshing all data...");
            refreshAllData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (transactionRepository != null) {
            transactionRepository.shutdown();
        }
        if (userRepository != null) {
            userRepository.shutdown();
        }
        

    }
}
