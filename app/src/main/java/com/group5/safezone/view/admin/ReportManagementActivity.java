package com.group5.safezone.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.adapter.AdminReportAdapter;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.dao.OrderDao;
import com.group5.safezone.model.dao.ProductDao;
import com.group5.safezone.model.dao.ReportDao;
import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.model.dao.TransactionsDao;
import com.group5.safezone.model.dao.NotificationDao;
import com.group5.safezone.model.entity.Notification;
import com.group5.safezone.model.entity.Order;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.Report;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.model.entity.Transactions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportManagementActivity extends AppCompatActivity {
    
    // UI Components
    private ImageButton btnBack;
    private EditText etSearch;
    private RecyclerView rvReports;
    private LinearLayout llLoading, llEmptyState;
    private TextView tvTotalReports, tvPendingReports;
    
    // Data
    private List<Report> allReports;
    private List<Report> filteredReports;
    private List<Order> orders;
    private List<Product> products;
    private List<User> buyers, sellers;
    
    // Database
    private ReportDao reportDao;
    private OrderDao orderDao;
    private ProductDao productDao;
    private UserDao userDao;
    private TransactionsDao transactionsDao;
    private NotificationDao notificationDao;
    private ExecutorService executor;
    private SessionManager sessionManager;
    
    // Adapter
    private AdminReportAdapter reportAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_management);
        
        initViews();
        setupToolbar();
        setupDatabase();
        setupRecyclerView();
        setupSearch();
        setupListeners();
        loadReports();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        rvReports = findViewById(R.id.rvReports);
        llLoading = findViewById(R.id.llLoading);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvTotalReports = findViewById(R.id.tvTotalReports);
        tvPendingReports = findViewById(R.id.tvPendingReports);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quản lý báo cáo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    private void setupDatabase() {
        AppDatabase db = AppDatabase.getDatabase(this);
        reportDao = db.reportDao();
        orderDao = db.orderDao();
        productDao = db.productDao();
        userDao = db.userDao();
        transactionsDao = db.transactionsDao();
        notificationDao = db.notificationDao();
        executor = Executors.newSingleThreadExecutor();
        sessionManager = new SessionManager(this);
    }
    
    private void setupRecyclerView() {
        reportAdapter = new AdminReportAdapter(this, new ArrayList<>(), new ArrayList<>(), 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(reportAdapter);
        
        // Set click listener for report actions
        reportAdapter.setOnReportActionListener(new AdminReportAdapter.OnReportActionListener() {
            @Override
            public void onSellerWin(Report report) {
                showSellerWinDialog(report);
            }
            
            @Override
            public void onBuyerWin(Report report) {
                showBuyerWinDialog(report);
            }
        });
    }
    
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void loadReports() {
        showLoadingState();
        
        executor.execute(() -> {
            try {
                // Load reports with status "Cần admin xử lý"
                allReports = reportDao.getReportsByStatus("Cần admin xử lý");
                
                if (allReports != null && !allReports.isEmpty()) {
                    // Load related data
                    orders = new ArrayList<>();
                    products = new ArrayList<>();
                    buyers = new ArrayList<>();
                    sellers = new ArrayList<>();
                    
                    for (Report report : allReports) {
                        // Load order
                        Order order = orderDao.getOrderById(report.getOrderId());
                        if (order != null) {
                            orders.add(order);
                            
                            // Load product
                            Product product = productDao.getProductById(order.getProductId());
                            if (product != null) {
                                products.add(product);
                                
                                // Load buyer and seller
                                User buyer = userDao.getUserById(order.getUserId());
                                User seller = userDao.getUserById(product.getUserId());
                                
                                if (buyer != null) buyers.add(buyer);
                                if (seller != null) sellers.add(seller);
                            }
                        }
                    }
                    
                    filteredReports = new ArrayList<>(allReports);
                    
                    runOnUiThread(() -> {
                        updateSummaryUI();
                        updateReportsList();
                        showReportsList();
                    });
                } else {
                    runOnUiThread(() -> {
                        showEmptyState();
                    });
                }
            } catch (Exception e) {
                Log.e("ReportManagement", "Error loading reports: " + e.getMessage());
                runOnUiThread(() -> {
                    showEmptyState();
                });
            }
        });
    }
    
    private void filterReports(String query) {
        if (allReports == null) return;
        
        if (query.trim().isEmpty()) {
            filteredReports = new ArrayList<>(allReports);
        } else {
            filteredReports = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            
            for (int i = 0; i < allReports.size(); i++) {
                Report report = allReports.get(i);
                Order order = orders.get(i);
                Product product = products.get(i);
                User buyer = buyers.get(i);
                User seller = sellers.get(i);
                
                // Search in report description
                if (report.getDescribe() != null && 
                    report.getDescribe().toLowerCase().contains(lowerQuery)) {
                    filteredReports.add(report);
                    continue;
                }
                
                // Search in order ID
                if (String.valueOf(order.getId()).contains(query)) {
                    filteredReports.add(report);
                    continue;
                }
                
                // Search in product name
                if (product.getProductName() != null && 
                    product.getProductName().toLowerCase().contains(lowerQuery)) {
                    filteredReports.add(report);
                    continue;
                }
                
                // Search in buyer/seller username
                if ((buyer != null && buyer.getUserName() != null && 
                     buyer.getUserName().toLowerCase().contains(lowerQuery)) ||
                    (seller != null && seller.getUserName() != null && 
                     seller.getUserName().toLowerCase().contains(lowerQuery))) {
                    filteredReports.add(report);
                }
            }
        }
        
        updateReportsList();
    }
    
    private void updateSummaryUI() {
        if (allReports == null) return;
        
        int totalReports = allReports.size();
        int pendingReports = filteredReports != null ? filteredReports.size() : 0;
        
        tvTotalReports.setText("Tổng: " + totalReports);
        tvPendingReports.setText("Đang xử lý: " + pendingReports);
    }
    
    private void updateReportsList() {
        if (reportAdapter != null && filteredReports != null) {
            // Get corresponding data for filtered reports
            List<Order> filteredOrders = new ArrayList<>();
            List<Product> filteredProducts = new ArrayList<>();
            List<User> filteredBuyers = new ArrayList<>();
            List<User> filteredSellers = new ArrayList<>();
            
            for (Report report : filteredReports) {
                int index = allReports.indexOf(report);
                if (index >= 0 && index < orders.size()) {
                    filteredOrders.add(orders.get(index));
                    filteredProducts.add(products.get(index));
                    filteredBuyers.add(buyers.get(index));
                    filteredSellers.add(sellers.get(index));
                }
            }
            
            reportAdapter.updateData(filteredReports, filteredOrders, filteredProducts, 
                filteredBuyers, filteredSellers);
        }
    }
    
    private void showLoadingState() {
        llLoading.setVisibility(View.VISIBLE);
        rvReports.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);
    }
    
    private void showReportsList() {
        llLoading.setVisibility(View.GONE);
        rvReports.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        llLoading.setVisibility(View.GONE);
        rvReports.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
    }
    
    private void showSellerWinDialog(Report report) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận quyết định")
            .setMessage("Bạn có chắc chắn muốn quyết định NGƯỜI BÁN THẮNG?\n\n" +
                "⚠️ Người bán sẽ nhận được 95% giá trị đơn hàng\n" +
                "⚠️ Người mua sẽ mất tiền\n" +
                "⚠️ Cả hai bên sẽ nhận được thông báo\n" +
                "⚠️ Quyết định này không thể thay đổi!")
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                processSellerWin(report);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void showBuyerWinDialog(Report report) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận quyết định")
            .setMessage("Bạn có chắc chắn muốn quyết định NGƯỜI MUA THẮNG?\n\n" +
                "⚠️ Người mua sẽ nhận được 95% giá trị đơn hàng\n" +
                "⚠️ Người bán sẽ mất tiền\n" +
                "⚠️ Cả hai bên sẽ nhận được thông báo\n" +
                "⚠️ Quyết định này không thể thay đổi!")
            .setPositiveButton("Xác nhận", (dialog, which )-> {
                processBuyerWin(report);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void processSellerWin(Report report) {
        executor.execute(() -> {
            try {
                // Find corresponding order and product
                int reportIndex = allReports.indexOf(report);
                if (reportIndex >= 0 && reportIndex < orders.size()) {
                    Order order = orders.get(reportIndex);
                    Product product = products.get(reportIndex);
                    User seller = sellers.get(reportIndex);
                    
                    if (order != null && product != null && seller != null) {
                        // Calculate 95% of order amount
                        double sellerPayment = order.getPrice() * 0.95;
                        
                        // Add money to seller's balance
                        double newSellerBalance = seller.getBalance() + sellerPayment;
                        seller.setBalance(newSellerBalance);
                        userDao.updateUser(seller);
                        
                        // Create transaction record
                        Transactions sellerTransaction = new Transactions();
                        sellerTransaction.setUserId(seller.getId());
                        sellerTransaction.setOrderId((int) order.getId());
                        sellerTransaction.setAmount(sellerPayment);
                        sellerTransaction.setTransactionType("Admin xử lý khiếu nại - Người bán thắng");
                        sellerTransaction.setDescription("Nhận 95% tiền đơn hàng #" + order.getId() + 
                            " do admin quyết định người bán thắng");
                        sellerTransaction.setStatus("Thành công");
                        sellerTransaction.setCreatedAt(new java.util.Date());
                        transactionsDao.insert(sellerTransaction);
                        
                        // Update report status
                        report.setStatus("Admin đã xử lý");
                        report.setUpdatedAt(new java.util.Date());
                        reportDao.update(report);
                        
                        // Update product status
                        product.setStatus("Đã xử lý khiếu nại");
                        productDao.update(product);
                        
                        // Create notification for seller
                        createNotification(seller.getId(), "ADMIN_DECISION_SELLER_WIN", 
                            "Admin đã quyết định bạn thắng trong khiếu nại đơn hàng #" + order.getId() + 
                            ". Bạn được hoàn trả 95% tiền đơn hàng: " + formatPrice(sellerPayment), "COMPLAINT", (int) order.getId());
                        
                        // Create notification for buyer
                        createNotification(order.getUserId(), "ADMIN_DECISION_SELLER_WIN", 
                            "Admin đã quyết định người bán thắng trong khiếu nại đơn hàng #" + order.getId() 
                        , "COMPLAINT", (int) order.getId());
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã xử lý thành công! Người bán nhận " + 
                                formatPrice(sellerPayment), Toast.LENGTH_LONG).show();
                            loadReports(); // Reload data
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("ReportManagement", "Error processing seller win: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi xử lý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void processBuyerWin(Report report) {
        executor.execute(() -> {
            try {
                // Find corresponding order and product
                int reportIndex = allReports.indexOf(report);
                if (reportIndex >= 0 && reportIndex < orders.size()) {
                    Order order = orders.get(reportIndex);
                    Product product = products.get(reportIndex);
                    User buyer = buyers.get(reportIndex);
                    
                    if (order != null && product != null && buyer != null) {
                        // Calculate 95% of order amount
                        double buyerRefund = order.getPrice() * 0.95;
                        
                        // Add money to buyer's balance
                        double newBuyerBalance = buyer.getBalance() + buyerRefund;
                        buyer.setBalance(newBuyerBalance);
                        userDao.updateUser(buyer);
                        
                        // Create transaction record
                        Transactions buyerTransaction = new Transactions();
                        buyerTransaction.setUserId(buyer.getId());
                        buyerTransaction.setOrderId((int) order.getId());
                        buyerTransaction.setAmount(buyerRefund);
                        buyerTransaction.setTransactionType("Admin xử lý khiếu nại - Người mua thắng");
                        buyerTransaction.setDescription("Nhận 95% tiền đơn hàng #" + order.getId() + 
                            " do admin quyết định người mua thắng");
                        buyerTransaction.setStatus("Thành công");
                        buyerTransaction.setCreatedAt(new java.util.Date());
                        transactionsDao.insert(buyerTransaction);
                        
                        // Update report status
                        report.setStatus("Admin đã xử lý");
                        report.setUpdatedAt(new java.util.Date());
                        reportDao.update(report);
                        
                        // Update product status
                        product.setStatus("Đã xử lý khiếu nại");
                        productDao.update(product);
                        
                        // Create notification for buyer
                        createNotification(buyer.getId(), "ADMIN_DECISION_BUYER_WIN", 
                            "Admin đã quyết định bạn thắng trong khiếu nại đơn hàng #" + order.getId() + 
                            ". Bạn nhận được " + formatPrice(buyerRefund), "COMPLAINT", (int) order.getId());
                        
                        // Create notification for seller
                        createNotification(product.getUserId(), "ADMIN_DECISION_BUYER_WIN", 
                            "Admin đã quyết định người mua thắng trong khiếu nại đơn hàng #" + order.getId() + 
                            ". Bạn mất " + formatPrice(order.getPrice()), "COMPLAINT", (int) order.getId());
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã xử lý thành công! Người mua nhận " + 
                                formatPrice(buyerRefund), Toast.LENGTH_LONG).show();
                            loadReports(); // Reload data
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("ReportManagement", "Error processing buyer win: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi xử lý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private String formatPrice(double price) {
        return String.format("%,.0f VNĐ", price);
    }
    
    private void createNotification(int userId, String type, String message, String relatedEntityType, int relatedEntityId) {
        executor.execute(() -> {
            try {
                Notification n = new Notification();
                n.setUserId(userId);
                n.setType(type);
                n.setMessage(message);
                n.setRelatedEntityType(relatedEntityType);
                n.setRelatedEntityId(relatedEntityId);
                n.setCreatedBy(sessionManager.getUserId());
                
                notificationDao.insert(n);
                Log.d("ReportManagement", "Created notification for user " + userId + ": " + message);
            } catch (Exception e) {
                Log.e("ReportManagement", "Error creating notification: " + e.getMessage());
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
