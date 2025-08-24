package com.group5.safezone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.adapter.OrderAdapter;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.dao.OrderDao;
import com.group5.safezone.model.dao.ProductDao;
import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.model.entity.Order;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PurchaseHistoryActivity extends AppCompatActivity {
    
    // UI Components
    private ImageButton btnBack;
    private TextView tvTotalOrders, tvTotalSpent, tvPendingOrders, tvCompletedOrders, tvOrderCount;
    private LinearLayout llLoading, llEmptyState;
    private RecyclerView rvOrders;
    
    // Data
    private List<Order> orders;
    private List<Product> products;
    private List<String> sellerUsernames;
    private int currentUserId;
    
    // Database
    private OrderDao orderDao;
    private ProductDao productDao;
    private UserDao userDao;
    private ExecutorService executor;
    private SessionManager sessionManager;
    
    // Adapter
    private OrderAdapter orderAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history);
        
        initViews();
        setupDatabase();
        setupRecyclerView();
        setupListeners();
        loadPurchaseHistory();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        llLoading = findViewById(R.id.llLoading);
        llEmptyState = findViewById(R.id.llEmptyState);
        rvOrders = findViewById(R.id.rvOrders);
    }
    
    private void setupDatabase() {
        AppDatabase db = AppDatabase.getDatabase(this);
        orderDao = db.orderDao();
        productDao = db.productDao();
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
    }
    
    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter();
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void loadPurchaseHistory() {
        showLoadingState();
        
        executor.execute(() -> {
            try {
                // Load orders for current user
                List<Order> userOrders = orderDao.getOrdersByUserId(currentUserId);
                
                if (userOrders != null && !userOrders.isEmpty()) {
                    // Load related products and seller usernames
                    List<Product> orderProducts = new ArrayList<>();
                    List<String> usernames = new ArrayList<>();
                    
                    for (Order order : userOrders) {
                        Product product = productDao.getProductById(order.getProductId());
                        if (product != null) {
                            orderProducts.add(product);
                            
                            // Get seller username
                            User seller = userDao.getUserById(product.getUserId());
                            usernames.add(seller != null ? seller.getUserName() : "Unknown");
                        }
                    }
                    
                    // Update UI on main thread
                    runOnUiThread(() -> {
                        this.orders = userOrders;
                        this.products = orderProducts;
                        this.sellerUsernames = usernames;
                        
                        updateSummaryUI();
                        updateOrdersList();
                        showOrdersList();
                    });
                } else {
                    runOnUiThread(() -> {
                        showEmptyState();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showEmptyState();
                });
            }
        });
    }
    
    private void updateSummaryUI() {
        if (orders == null || orders.isEmpty()) return;
        
        int totalOrders = orders.size();
        double totalSpent = 0;
        int pendingOrders = 0;
        int completedOrders = 0;
        
        for (Order order : orders) {
            totalSpent += order.getPrice();
            
            if ("chờ người mua xác nhận nhận".equals(order.getStatus())) {
                pendingOrders++;
            } else if ("đã hoàn thành".equals(order.getStatus())) {
                completedOrders++;
            }
        }
        
        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvTotalSpent.setText(formatPrice(totalSpent));
        tvPendingOrders.setText(String.valueOf(pendingOrders));
        tvCompletedOrders.setText(String.valueOf(completedOrders));
        tvOrderCount.setText(totalOrders + " đơn hàng");
    }
    
    private void updateOrdersList() {
        if (orderAdapter != null && orders != null && products != null && sellerUsernames != null) {
            orderAdapter.updateData(orders, products, sellerUsernames);
        }
    }
    
    private void showLoadingState() {
        llLoading.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);
    }
    
    private void showOrdersList() {
        llLoading.setVisibility(View.GONE);
        rvOrders.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        llLoading.setVisibility(View.GONE);
        rvOrders.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
    }
    
    private String formatPrice(double price) {
        return String.format("%,.0f VNĐ", price);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity
        loadPurchaseHistory();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // OrderDetailActivity returned with changes, refresh data
            loadPurchaseHistory();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
