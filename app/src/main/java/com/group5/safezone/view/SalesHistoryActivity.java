package com.group5.safezone.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.adapter.SalesOrderAdapter;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.dao.OrderDao;
import com.group5.safezone.model.dao.ProductDao;
// ProductImagesDao import removed - no image loading needed
import com.group5.safezone.model.dao.ReportDao;
import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.model.entity.Order;
import com.group5.safezone.model.entity.Product;
// ProductImages entity import removed - no image loading needed
import com.group5.safezone.model.entity.Report;
import com.group5.safezone.model.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SalesHistoryActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack, btnFilter;
    private EditText etSearchOrder;
    private TextView tvTotalSales, tvTotalOrders;
    private RecyclerView rvSalesOrders;
    private LinearLayout layoutEmptyState;

    // Data
    private List<Order> allOrders;
    private List<Order> filteredOrders;
    private List<Product> products;
    private List<User> buyers;
    // productImages field removed - no image loading needed
    private List<Report> reports;

    // Database
    private OrderDao orderDao;
    private ProductDao productDao;
    private UserDao userDao;
    // productImagesDao field removed - no image loading needed
    private ReportDao reportDao;

    // Current user
    private int currentUserId;

    // Executor for background tasks
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_history);

        initViews();
        initDatabase();
        loadCurrentUser();
        setupClickListeners();
        setupSearch();
        loadSalesData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilter);
        etSearchOrder = findViewById(R.id.etSearchOrder);
        tvTotalSales = findViewById(R.id.tvTotalSales);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        rvSalesOrders = findViewById(R.id.rvSalesOrders);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        // Setup RecyclerView
        rvSalesOrders.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initDatabase() {
        AppDatabase database = AppDatabase.getDatabase(this);
        orderDao = database.orderDao();
        productDao = database.productDao();
        userDao = database.userDao();
        // productImagesDao initialization removed - no image loading needed
        reportDao = database.reportDao();

        executor = Executors.newSingleThreadExecutor();
    }

    private void loadCurrentUser() {
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng lọc đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        etSearchOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSalesData() {
        executor.execute(() -> {
            try {

                // Load orders where current user is the seller
                List<Order> userOrders = new ArrayList<>();
                
                try {
                    // Sử dụng method mới để lấy đơn hàng theo seller ID
                    userOrders = orderDao.getOrdersBySellerId(currentUserId);
                } catch (Exception e) {
                    // Fallback: Lấy tất cả đơn hàng và kiểm tra xem user hiện tại có phải là người bán không
                    List<Order> allOrders = orderDao.getAllOrders();
                    
                    for (Order order : allOrders) {
                        Product product = productDao.getProductById(order.getProductId());
                        if (product != null && product.getUserId() == currentUserId) {
                            userOrders.add(order);
                        }
                    }
                }

                // Load related data
                products = new ArrayList<>();
                buyers = new ArrayList<>();
                reports = new ArrayList<>();

                for (Order order : userOrders) {
                    // Load product
                    Product product = productDao.getProductById(order.getProductId());
                    if (product != null) {
                        products.add(product);
                    }

                    // Load buyer
                    User buyer = userDao.getUserById(order.getUserId());
                    if (buyer != null) {
                        buyers.add(buyer);
                    }

                    // Product image loading removed - no image loading needed

                    // Load report if exists
                    Report report = reportDao.getReportByOrderId(order.getId());
                    if (report != null) {
                        reports.add(report);
                    }
                }

                allOrders = userOrders;
                filteredOrders = new ArrayList<>(userOrders);

                // Update UI on main thread
                runOnUiThread(() -> {
                    updateStatistics();
                    updateOrdersList();
                    checkEmptyState();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void filterOrders(String query) {
        if (query.isEmpty()) {
            filteredOrders = new ArrayList<>(allOrders);
        } else {
            filteredOrders = new ArrayList<>();
            String lowerQuery = query.toLowerCase();

            for (Order order : allOrders) {
                // Find related product
                Product product = findProductById(order.getProductId());
                if (product != null && product.getProductName().toLowerCase().contains(lowerQuery)) {
                    filteredOrders.add(order);
                    continue;
                }

                // Find related buyer
                User buyer = findUserById(order.getUserId());
                if (buyer != null && buyer.getUserName().toLowerCase().contains(lowerQuery)) {
                    filteredOrders.add(order);
                    continue;
                }

                // Check order ID
                if (String.valueOf(order.getId()).contains(query)) {
                    filteredOrders.add(order);
                }
            }
        }

        updateOrdersList();
        checkEmptyState();
    }

    private void updateStatistics() {
        double totalSales = 0;
        for (Order order : allOrders) {
            totalSales += order.getPrice();
        }

        tvTotalSales.setText(formatPrice(totalSales));
        tvTotalOrders.setText(String.valueOf(allOrders.size()));
    }

    private void updateOrdersList() {
        SalesOrderAdapter adapter = new SalesOrderAdapter(this, filteredOrders, products, buyers, reports);
        rvSalesOrders.setAdapter(adapter);
    }

    private void checkEmptyState() {
        if (filteredOrders.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvSalesOrders.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvSalesOrders.setVisibility(View.VISIBLE);
        }
    }

    private Product findProductById(String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    private User findUserById(int userId) {
        for (User user : buyers) {
            if (user.getId() == userId) {
                return user;
            }
        }
        return null;
    }

    private String formatPrice(double price) {
        return String.format("%,.0f VNĐ", price);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from OrderDetailActivity
        loadSalesData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
