package com.group5.safezone.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.group5.safezone.R;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.dao.OrderDao;
import com.group5.safezone.model.dao.ProductDao;
import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.model.dao.ReportDao;
import com.group5.safezone.model.dao.NotificationDao;
import com.group5.safezone.model.entity.Order;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.model.entity.Report;
import com.group5.safezone.model.entity.Notification;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderDetailActivity extends AppCompatActivity {
    
    // Data
    private Order order;
    private Product product;
    private int currentUserId;
    private Report existingReport;
    
    // Database
    private OrderDao orderDao;
    private ProductDao productDao;
    private UserDao userDao;
    private ReportDao reportDao;
    private NotificationDao notificationDao;
    private ExecutorService executor;
    private SessionManager sessionManager;
    
    // Constants
    public static final String EXTRA_ORDER_ID = "order_id";
    
    public static Intent newIntent(Context context, long orderId) {
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra(EXTRA_ORDER_ID, orderId);
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        
        initViews();
        setupDatabase();
        
        // Check if user is the buyer
        if (!validateBuyerAccess()) {
            return;
        }
        
        loadOrderData();
        setupListeners();
    }
    
    private void initViews() {
        // Find views if they exist, ignore if not
        try {
            findViewById(R.id.btnBack);
        } catch (Exception e) {
            // Layout might be simple, just continue
        }
        
        // Create a simple layout programmatically if needed
        createSimpleLayout();
    }
    
    private void createSimpleLayout() {
        // Just use the default layout - we'll populate it dynamically in displayOrderInfo
        // No need to create custom layout here
    }
    
    private void setupListeners() {
        // Setup basic back button if exists
        try {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        } catch (Exception e) {
            // No back button, ignore
        }
    }
    
    private void setupDatabase() {
        AppDatabase db = AppDatabase.getDatabase(this);
        orderDao = db.orderDao();
        productDao = db.productDao();
        userDao = db.userDao();
        reportDao = db.reportDao();
        notificationDao = db.notificationDao();
        executor = Executors.newSingleThreadExecutor();
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
    }
    
    private boolean validateBuyerAccess() {
        long orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        
        // For now, allow access and validate in loadOrderData
        // This avoids blocking the UI thread
        return true;
    }
    
    private void loadOrderData() {
        long orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);
        
        // Debug logging
        Log.d("OrderDetail", "Received orderId: " + orderId);
        Log.d("OrderDetail", "EXTRA_ORDER_ID constant: " + EXTRA_ORDER_ID);
        
        executor.execute(() -> {
            try {
                // Load order
                order = orderDao.getOrderById((int) orderId);
                if (order == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                
                // Load product
                product = productDao.getProductById(order.getProductId());
                if (product == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                
                // Load existing report for this order
                existingReport = reportDao.getReportByOrderId((int) orderId);
                
                // Debug logging for report
                Log.d("OrderDetail", "Loaded report: " + (existingReport != null ? 
                    "ID=" + existingReport.getId() + 
                    ", Status=" + existingReport.getStatus() + 
                    ", OrderId=" + existingReport.getOrderId() : "NULL"));
                
                runOnUiThread(() -> {
                    displayOrderInfo();
                    Toast.makeText(this, "Đã tải dữ liệu đơn hàng thành công", Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                Log.e("OrderDetail", "Error loading order data: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void displayOrderInfo() {
        if (order == null || product == null) {
            Log.e("OrderDetail", "Order or product is null");
            return;
        }
        
        try {
            // Find the content container and cast to ViewGroup
            android.view.ViewGroup contentContainer = findViewById(android.R.id.content);
            if (contentContainer == null) {
                Log.e("OrderDetail", "Content container not found");
                return;
            }
            
            // Clear existing content
            contentContainer.removeAllViews();
            
            // Create ScrollView for scrollable content
            android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
            
            // Create our main LinearLayout
            android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
            mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            mainLayout.setPadding(24, 24, 24, 24);
            mainLayout.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            
            // Add header with title and back button
            android.widget.LinearLayout headerLayout = new android.widget.LinearLayout(this);
            headerLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            headerLayout.setPadding(16, 16, 16, 24);
            headerLayout.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
            headerLayout.setElevation(4);
            
            // Back button (icon style)
            android.widget.Button backButton = new android.widget.Button(this);
            backButton.setText("←");
            backButton.setTextSize(20);
            backButton.setTextColor(android.graphics.Color.parseColor("#1976D2"));
            backButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            backButton.setPadding(16, 16, 16, 16);
            backButton.setOnClickListener(v -> finish());
            android.widget.LinearLayout.LayoutParams backButtonParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
            backButton.setLayoutParams(backButtonParams);
            headerLayout.addView(backButton);
            
            // Title
            android.widget.TextView titleView = new android.widget.TextView(this);
            titleView.setText("📦 Chi tiết đơn hàng");
            titleView.setTextSize(20);
            titleView.setTextColor(android.graphics.Color.parseColor("#1976D2"));
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
            titleView.setPadding(16, 16, 16, 16);
            android.widget.LinearLayout.LayoutParams titleParams = new android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            );
            titleView.setLayoutParams(titleParams);
            headerLayout.addView(titleView);
            
            mainLayout.addView(headerLayout);
            
            // Add main layout to scroll view
            scrollView.addView(mainLayout);
            
            // Add scroll view to content container
            contentContainer.addView(scrollView);
            
            // Add order information section
            addInfoSection(mainLayout, "📦 THÔNG TIN ĐƠN HÀNG", new String[]{
                "🆔 Mã đơn hàng: #" + order.getId(),
                "📅 Ngày đặt: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(order.getOrderDate()),
                "📊 Trạng thái: " + order.getStatus(),
                "💰 Giá: " + formatPrice(order.getPrice())
            });
            
            // Add product information section
            addInfoSection(mainLayout, "🛍️ THÔNG TIN SẢN PHẨM", new String[]{
                "🏷️ Tên sản phẩm: " + product.getProductName(),
                "📝 Mô tả: " + (product.getDescribe() != null ? product.getDescribe() : "Không có mô tả"),
                "ℹ️ Thông tin: " + (product.getInformation() != null ? product.getInformation() : "Không có thông tin chi tiết")
            });
            
            // Add private information section if exists
            if (product.getPrivateInfo() != null && !product.getPrivateInfo().trim().isEmpty()) {
                String privateInfo;
                try {
                    // Try to decrypt private info
                    privateInfo = com.group5.safezone.config.PasswordUtils.decrypt(product.getPrivateInfo());
                } catch (Exception e) {
                    // If decryption fails, show as plain text
                    privateInfo = product.getPrivateInfo();
                }
                
                addInfoSection(mainLayout, "🔒 THÔNG TIN RIÊNG TƯ", new String[]{privateInfo});
            }
            
            // Add note section if exists
            if (order.getNote() != null && !order.getNote().trim().isEmpty()) {
                addInfoSection(mainLayout, "📝 GHI CHÚ", new String[]{order.getNote()});
            }
            
            // Add complaint handling section if there's a complaint (only visible to seller)
            Log.d("OrderDetail", "Checking complaint: existingReport=" + (existingReport != null ? "NOT NULL" : "NULL") + 
                ", status=" + (existingReport != null ? existingReport.getStatus() : "N/A"));
            
            if (existingReport != null && 
                ("Khiếu nại".equals(existingReport.getStatus()) || "Khiếu Nại".equals(existingReport.getStatus()) || "Cần admin xử lý".equals(existingReport.getStatus())) &&
                product.getUserId() == currentUserId) { // Only show to seller
                Log.d("OrderDetail", "Adding complaint section - status matches and user is seller!");
                addComplaintSection(mainLayout);
            } else {
                Log.d("OrderDetail", "No complaint section added - status doesn't match, report is null, or user is not seller");
            }
            
            // Add action buttons for buyer (only if order is pending, user is the buyer, and no complaint exists)
            if (order.getUserId() == currentUserId && 
                "chờ người mua xác nhận nhận".equals(order.getStatus()) && 
                existingReport == null) {
                addBuyerActionButtons(mainLayout);
            }
            
        } catch (Exception e) {
            Log.e("OrderDetail", "Error displaying order info: " + e.getMessage());
            Toast.makeText(this, "Lỗi hiển thị thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addInfoSection(android.widget.LinearLayout parent, String title, String[] items) {
        // Create section container with card style
        android.widget.LinearLayout sectionContainer = new android.widget.LinearLayout(this);
        sectionContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        sectionContainer.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
        sectionContainer.setElevation(2);
        sectionContainer.setPadding(20, 20, 20, 20);
        
        // Set margins for the card
        android.widget.LinearLayout.LayoutParams sectionParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        sectionParams.setMargins(0, 16, 0, 0);
        sectionContainer.setLayoutParams(sectionParams);
        
        // Add section title
        android.widget.TextView titleView = new android.widget.TextView(this);
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setTextColor(android.graphics.Color.parseColor("#1976D2"));
        titleView.setPadding(0, 0, 0, 16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        sectionContainer.addView(titleView);
        
        // Add divider line
        android.view.View divider = new android.view.View(this);
        divider.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        android.widget.LinearLayout.LayoutParams dividerParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            2
        );
        dividerParams.setMargins(0, 0, 0, 16);
        divider.setLayoutParams(dividerParams);
        sectionContainer.addView(divider);
        
        // Add section content
        for (String item : items) {
            android.widget.TextView itemView = new android.widget.TextView(this);
            itemView.setText(item);
            itemView.setTextSize(15);
            itemView.setTextColor(android.graphics.Color.parseColor("#424242"));
            itemView.setPadding(0, 8, 0, 8);
            itemView.setLineSpacing(4, 1.2f);
            sectionContainer.addView(itemView);
        }
        
        parent.addView(sectionContainer);
    }
    
    private void addComplaintSection(android.widget.LinearLayout parent) {
        // Create complaint section container
        android.widget.LinearLayout complaintContainer = new android.widget.LinearLayout(this);
        complaintContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        complaintContainer.setBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"));
        complaintContainer.setElevation(3);
        complaintContainer.setPadding(20, 20, 20, 20);
        
        // Set margins for the card
        android.widget.LinearLayout.LayoutParams complaintParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        complaintParams.setMargins(0, 24, 0, 0);
        complaintContainer.setLayoutParams(complaintParams);
        
        // Add complaint title based on status
        android.widget.TextView complaintTitle = new android.widget.TextView(this);
        if ("Cần admin xử lý".equals(existingReport.getStatus())) {
            complaintTitle.setText("⏳ KHIẾU NẠI ĐANG CHỜ ADMIN XỬ LÝ");
            complaintTitle.setTextColor(android.graphics.Color.parseColor("#9E9E9E")); // Grey color
        } else {
            complaintTitle.setText("⚠️ KHIẾU NẠI CẦN XỬ LÝ");
            complaintTitle.setTextColor(android.graphics.Color.parseColor("#F57C00")); // Orange color
        }
        complaintTitle.setTextSize(18);
        complaintTitle.setPadding(0, 0, 0, 16);
        complaintTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        complaintContainer.addView(complaintTitle);
        
        // Add complaint info
        if (existingReport != null) {
            android.widget.TextView complaintInfo = new android.widget.TextView(this);
            String complaintText = "📝 Nội dung khiếu nại: " + (existingReport.getDescribe() != null ? existingReport.getDescribe() : "Không có mô tả");
            if (existingReport.getCreatedAt() != null) {
                complaintText += "\n📅 Ngày khiếu nại: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(existingReport.getCreatedAt());
            }
            complaintInfo.setText(complaintText);
            complaintInfo.setTextSize(15);
            complaintInfo.setTextColor(android.graphics.Color.parseColor("#424242"));
            complaintInfo.setPadding(0, 0, 0, 20);
            complaintInfo.setLineSpacing(4, 1.2f);
            complaintContainer.addView(complaintInfo);
        }
        
        // Add buttons container only if status is not "Cần admin xử lý"
        if (!"Cần admin xử lý".equals(existingReport.getStatus())) {
            android.widget.LinearLayout buttonsContainer = new android.widget.LinearLayout(this);
            buttonsContainer.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            buttonsContainer.setPadding(0, 16, 0, 0);
            
            // Accept button
            android.widget.Button acceptButton = new android.widget.Button(this);
            acceptButton.setText("✅ Đồng ý");
            acceptButton.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
            acceptButton.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
            acceptButton.setPadding(24, 16, 24, 16);
            acceptButton.setOnClickListener(v -> handleComplaintResponse(true));
            
            android.widget.LinearLayout.LayoutParams acceptParams = new android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            );
            acceptParams.setMargins(0, 0, 8, 0);
            acceptButton.setLayoutParams(acceptParams);
            buttonsContainer.addView(acceptButton);
            
            // Reject button  
            android.widget.Button rejectButton = new android.widget.Button(this);
            rejectButton.setText("❌ Không đồng ý");
            rejectButton.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
            rejectButton.setBackgroundColor(android.graphics.Color.parseColor("#F44336"));
            rejectButton.setPadding(24, 16, 24, 16);
            rejectButton.setOnClickListener(v -> handleComplaintResponse(false));
            
            android.widget.LinearLayout.LayoutParams rejectParams = new android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            );
            rejectParams.setMargins(8, 0, 0, 0);
            rejectButton.setLayoutParams(rejectParams);
            buttonsContainer.addView(rejectButton);
            
            complaintContainer.addView(buttonsContainer);
        } else {
            // Add info text for admin processing status
            android.widget.TextView adminInfo = new android.widget.TextView(this);
            adminInfo.setText("📋 Khiếu nại đã được chuyển cho admin xử lý.\n⏰ Vui lòng chờ kết quả từ admin.");
            adminInfo.setTextSize(15);
            adminInfo.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
            adminInfo.setPadding(0, 16, 0, 0);
            adminInfo.setLineSpacing(4, 1.2f);
            complaintContainer.addView(adminInfo);
        }
        parent.addView(complaintContainer);
    }
    
    private void handleComplaintResponse(boolean accepted) {
        if (existingReport == null) {
            Toast.makeText(this, "Không tìm thấy thông tin khiếu nại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String action = accepted ? "chấp nhận" : "từ chối";
        String message = "Bạn có chắc chắn muốn " + action + " khiếu nại này không?";
        
        if (accepted) {
            message += "\n\n⚠️ Lưu ý: Nếu đồng ý, người mua sẽ được hoàn 95% tiền sản phẩm!";
        } else {
            message += "\n\n⚠️ Lưu ý: Nếu từ chối, khiếu nại sẽ được chuyển cho admin xử lý!";
        }
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xử lý khiếu nại")
            .setMessage(message)
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                // Process complaint response
                executor.execute(() -> {
                    try {
                        if (accepted) {
                            processAcceptedComplaint();
                        } else {
                            processRejectedComplaint();
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Lỗi xử lý khiếu nại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void processAcceptedComplaint() {
        try {
            // Calculate 95% refund amount
            double refundAmount = order.getPrice() * 0.95;
            
            // Update buyer's balance (add refund)
            User buyer = userDao.getUserById(order.getUserId());
            if (buyer != null) {
                double newBalance = buyer.getBalance() + refundAmount;
                buyer.setBalance(newBalance);
                userDao.updateUser(buyer);
                
                // Create transaction record for refund
                com.group5.safezone.model.entity.Transactions refundTransaction = new com.group5.safezone.model.entity.Transactions();
                refundTransaction.setUserId(order.getUserId()); // Buyer
                refundTransaction.setRelatedUserId(currentUserId); // Seller
                refundTransaction.setOrderId(order.getId());
                refundTransaction.setAmount(refundAmount);
                refundTransaction.setTransactionType("Hoàn tiền khiếu nại");
                refundTransaction.setDescription("Hoàn 95% tiền sản phẩm do khiếu nại được chấp nhận - Đơn hàng #" + order.getId());
                refundTransaction.setStatus("Thành công");
                refundTransaction.setCreatedAt(new java.util.Date());
                
                // Get TransactionsDao
                com.group5.safezone.model.dao.TransactionsDao transactionsDao = AppDatabase.getDatabase(this).transactionsDao();
                transactionsDao.insert(refundTransaction);
                
                // Update report status
                existingReport.setStatus("Đã chấp nhận");
                existingReport.setUpdatedAt(new java.util.Date());
                reportDao.update(existingReport);
                
                // Show success message
                runOnUiThread(() -> {
                    String successMsg = "✅ Đã chấp nhận khiếu nại thành công!\n\n" +
                        "💰 Đã hoàn " + formatPrice(refundAmount) + " cho người mua\n" +
                        "📱 Người mua sẽ nhận được thông báo";
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("Thành công")
                        .setMessage(successMsg)
                        .setPositiveButton("Đóng", (dialog, which) -> {
                            // Show notification to buyer about complaint acceptance
                            showNotification("Khiếu nại đã được chấp nhận", "Khiếu nại của bạn đã được người bán chấp nhận và bạn sẽ nhận được hoàn tiền 95%.");
                            
                            // Create notification for buyer
                            createNotification(order.getUserId(), "COMPLAINT_ACCEPTED", 
                                "Khiếu nại của bạn đã được người bán chấp nhận và bạn sẽ nhận được hoàn tiền 95%", "COMPLAINT", (int) order.getId());
                            
                            // Reload the display
                            displayOrderInfo();
                        })
                        .show();
                });
                
            } else {
                throw new Exception("Không tìm thấy thông tin người mua");
            }
            
        } catch (Exception e) {
            Log.e("OrderDetail", "Error processing accepted complaint: " + e.getMessage());
            runOnUiThread(() -> {
                Toast.makeText(this, "Lỗi xử lý khiếu nại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void processRejectedComplaint() {
        try {
            // Update report status to "Cần admin xử lý"
            existingReport.setStatus("Cần admin xử lý");
            existingReport.setUpdatedAt(new java.util.Date());
            reportDao.update(existingReport);
            
                            // Show success message
                runOnUiThread(() -> {
                    String successMsg = "❌ Đã từ chối khiếu nại!\n\n" +
                        "📋 Khiếu nại đã được chuyển cho admin xử lý\n" +
                        "📱 Cả người mua và người bán sẽ nhận được thông báo";
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("Đã xử lý")
                        .setMessage(successMsg)
                        .setPositiveButton("Đóng", (dialog, which) -> {
                            // Show notification to buyer about complaint rejection
                            showNotification("Khiếu nại bị từ chối", "Khiếu nại của bạn đã bị người bán từ chối và đã được chuyển cho admin xử lý.");
                            
                            // Create notification for buyer
                            createNotification(order.getUserId(), "COMPLAINT_REJECTED", 
                                "Khiếu nại của bạn đã bị người bán từ chối và đã được chuyển cho admin xử lý", "COMPLAINT", (int) order.getId());
                            
                            // Reload the display
                            displayOrderInfo();
                        })
                        .show();
                });
            
        } catch (Exception e) {
            Log.e("OrderDetail", "Error processing rejected complaint: " + e.getMessage());
            runOnUiThread(() -> {
                Toast.makeText(this, "Lỗi xử lý khiếu nại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private String formatPrice(double price) {
        return String.format("%,.0f VNĐ", price);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    private void addBuyerActionButtons(android.widget.LinearLayout parent) {
        // Create action buttons container
        android.widget.LinearLayout buttonsContainer = new android.widget.LinearLayout(this);
        buttonsContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        buttonsContainer.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
        buttonsContainer.setElevation(3);
        buttonsContainer.setPadding(20, 20, 20, 20);
        
        // Set margins for the card
        android.widget.LinearLayout.LayoutParams buttonsParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonsParams.setMargins(0, 24, 0, 0);
        buttonsContainer.setLayoutParams(buttonsParams);
        
        // Add section title
        android.widget.TextView titleView = new android.widget.TextView(this);
        titleView.setText("🎯 HÀNH ĐỘNG CỦA NGƯỜI MUA");
        titleView.setTextSize(18);
        titleView.setTextColor(android.graphics.Color.parseColor("#1976D2"));
        titleView.setPadding(0, 0, 0, 16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        buttonsContainer.addView(titleView);
        
        // Add divider line
        android.view.View divider = new android.view.View(this);
        divider.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        android.widget.LinearLayout.LayoutParams dividerParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            2
        );
        dividerParams.setMargins(0, 0, 0, 16);
        divider.setLayoutParams(dividerParams);
        buttonsContainer.addView(divider);
        
        // Create buttons container
        android.widget.LinearLayout buttonRow = new android.widget.LinearLayout(this);
        buttonRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, 0, 0, 0);
        
        // Confirm Order button
        android.widget.Button btnConfirmOrder = new android.widget.Button(this);
        btnConfirmOrder.setText("✅ XÁC NHẬN ĐƠN HÀNG");
        btnConfirmOrder.setTextSize(14);
        btnConfirmOrder.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
        btnConfirmOrder.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
        btnConfirmOrder.setPadding(20, 12, 20, 12);
        
        android.widget.LinearLayout.LayoutParams confirmParams = new android.widget.LinearLayout.LayoutParams(
            0,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        confirmParams.setMargins(0, 0, 8, 0);
        btnConfirmOrder.setLayoutParams(confirmParams);
        
        btnConfirmOrder.setOnClickListener(v -> showConfirmOrderDialog());
        buttonRow.addView(btnConfirmOrder);
        
        // Complaint button
        android.widget.Button btnComplaint = new android.widget.Button(this);
        btnComplaint.setText("⚠️ KHIẾU NẠI");
        btnComplaint.setTextSize(14);
        btnComplaint.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
        btnComplaint.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"));
        btnComplaint.setPadding(20, 12, 20, 12);
        
        android.widget.LinearLayout.LayoutParams complaintParams = new android.widget.LinearLayout.LayoutParams(
            0,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        complaintParams.setMargins(8, 0, 0, 0);
        btnComplaint.setLayoutParams(complaintParams);
        
        btnComplaint.setOnClickListener(v -> showComplaintDialog());
        buttonRow.addView(btnComplaint);
        
        buttonsContainer.addView(buttonRow);
        parent.addView(buttonsContainer);
    }
    
    private void showConfirmOrderDialog() {
        String confirmMessage = "Bạn có chắc chắn đã nhận được hàng?\n\n" +
                "Đơn hàng: #" + order.getId() + "\n" +
                "Sản phẩm: " + product.getProductName() + "\n" +
                "Tổng tiền: " + formatPrice(order.getPrice()) + "\n\n" +
                "⚠️ Sau khi xác nhận, bạn không thể hoàn tác được!";
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận nhận hàng")
            .setMessage(confirmMessage)
            .setPositiveButton("Đã nhận hàng", (dialog, which) -> {
                confirmOrderReceived();
            })
            .setNegativeButton("Chưa nhận", null)
            .show();
    }
    
    private void confirmOrderReceived() {
        executor.execute(() -> {
            try {
                // Check product fee type and handle accordingly
                Double feeType = product.getFee();
                double orderAmount = order.getPrice();
                
                if (feeType != null) {
                    if (feeType == 1.0) {
                        // Fee = 1: Seller pays fee (95% to seller)
                        handleSellerFeePayment(orderAmount);
                    } else if (feeType == 2.0) {
                        // Fee = 2: Buyer pays fee (check balance first)
                        handleBuyerFeePayment(orderAmount);
                    } else {
                        // Unknown fee type, just complete order
                        completeOrder();
                    }
                } else {
                    // Fee is null, just complete order
                    completeOrder();
                }
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi xác nhận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void handleSellerFeePayment(double orderAmount) {
        try {
            // Calculate seller's payment (95% of order amount)
            double sellerPayment = orderAmount * 0.95;
            double serviceFee = orderAmount * 0.05;
            
            // Get seller user
            User seller = userDao.getUserById(product.getUserId());
            if (seller != null) {
                // Add 95% to seller's balance
                double newSellerBalance = seller.getBalance() + sellerPayment;
                seller.setBalance(newSellerBalance);
                userDao.updateUser(seller);
                
                // Create transaction record for seller
                com.group5.safezone.model.entity.Transactions sellerTransaction = new com.group5.safezone.model.entity.Transactions();
                sellerTransaction.setUserId(product.getUserId());
                sellerTransaction.setOrderId((int) order.getId());
                sellerTransaction.setAmount(sellerPayment);
                sellerTransaction.setTransactionType("Thanh toán đơn hàng");
                sellerTransaction.setDescription("Nhận 95% tiền đơn hàng #" + order.getId() + " - " + product.getProductName());
                sellerTransaction.setStatus("Thành công");
                sellerTransaction.setCreatedAt(new java.util.Date());
                
                com.group5.safezone.model.dao.TransactionsDao transactionsDao = AppDatabase.getDatabase(this).transactionsDao();
                transactionsDao.insert(sellerTransaction);
                
                // Complete the order
                completeOrder();
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã xác nhận đơn hàng! Người bán nhận " + formatPrice(sellerPayment), Toast.LENGTH_LONG).show();
                });
            } else {
                throw new Exception("Không tìm thấy thông tin người bán");
            }
            
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Lỗi xử lý thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void handleBuyerFeePayment(double orderAmount) {
        try {
            // Calculate required fee (5% of order amount)
            double requiredFee = orderAmount * 0.05;
            
            // Get buyer's current balance
            User buyer = userDao.getUserById(currentUserId);
            if (buyer != null) {
                double currentBalance = buyer.getBalance();
                
                if (currentBalance >= requiredFee) {
                    // Buyer has enough balance, deduct fee
                    double newBalance = currentBalance - requiredFee;
                    buyer.setBalance(newBalance);
                    userDao.updateUser(buyer);
                    
                    // Create transaction record for fee deduction
                    com.group5.safezone.model.entity.Transactions feeTransaction = new com.group5.safezone.model.entity.Transactions();
                    feeTransaction.setUserId(currentUserId);
                    feeTransaction.setOrderId((int) order.getId());
                    feeTransaction.setAmount(-requiredFee);
                    feeTransaction.setTransactionType("Phí dịch vụ");
                    feeTransaction.setDescription("Trừ phí 5% đơn hàng #" + order.getId() + " - " + product.getProductName());
                    feeTransaction.setStatus("Thành công");
                    feeTransaction.setCreatedAt(new java.util.Date());
                    
                    com.group5.safezone.model.dao.TransactionsDao transactionsDao = AppDatabase.getDatabase(this).transactionsDao();
                    transactionsDao.insert(feeTransaction);
                    
                    // Complete the order
                    completeOrder();
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã xác nhận đơn hàng! Đã trừ phí " + formatPrice(requiredFee), Toast.LENGTH_LONG).show();
                    });
                    
                } else {
                    // Buyer doesn't have enough balance
                    runOnUiThread(() -> {
                        String message = "Số dư không đủ để xác nhận đơn hàng!\n\n" +
                                "Cần: " + formatPrice(requiredFee) + "\n" +
                                "Hiện có: " + formatPrice(currentBalance) + "\n\n" +
                                "Vui lòng nạp thêm tiền để tiếp tục.";
                        
                        new android.app.AlertDialog.Builder(this)
                            .setTitle("Số dư không đủ")
                            .setMessage(message)
                            .setPositiveButton("Nạp tiền", (dialog, which) -> {
                                // TODO: Navigate to wallet or payment screen
                                Toast.makeText(this, "Chuyển đến trang nạp tiền", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                    });
                }
            } else {
                throw new Exception("Không tìm thấy thông tin người mua");
            }
            
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Lỗi kiểm tra số dư: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void completeOrder() {
        try {
            // Update order status to completed
            order.setStatus("đã hoàn thành");
            orderDao.update(order);
            
            // Show success message
                            runOnUiThread(() -> {
                    Toast.makeText(this, "Đã xác nhận nhận hàng thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Show notification to seller about order completion
                    showNotification("Đơn hàng đã hoàn thành", "Đơn hàng #" + order.getId() + " đã được người mua xác nhận nhận hàng.");
                    
                    // Create notification for seller
                    createNotification(product.getUserId(), "ORDER_COMPLETED", 
                        "Đơn hàng #" + order.getId() + " đã được người mua xác nhận nhận hàng", "ORDER", (int) order.getId());
                    
                    // Reload the display
                    displayOrderInfo();
                });
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Lỗi khi hoàn tất đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void showComplaintDialog() {
        // Create input dialog for complaint
        android.widget.EditText etComplaint = new android.widget.EditText(this);
        etComplaint.setHint("Nhập nội dung khiếu nại...");
        etComplaint.setMinLines(3);
        etComplaint.setMaxLines(5);
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Tạo khiếu nại")
            .setMessage("Vui lòng mô tả vấn đề bạn gặp phải:")
            .setView(etComplaint)
            .setPositiveButton("Gửi khiếu nại", (dialog, which) -> {
                String complaintText = etComplaint.getText().toString().trim();
                if (!complaintText.isEmpty()) {
                    createComplaint(complaintText);
                } else {
                    Toast.makeText(this, "Vui lòng nhập nội dung khiếu nại", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void createComplaint(String complaintText) {
        executor.execute(() -> {
            try {
                // Create new report
                Report report = new Report();
                report.setOrderId((int) order.getId());
                report.setCreatedBy(currentUserId); // User who created the complaint
                report.setDescribe(complaintText);
                report.setStatus("Khiếu nại");
                report.setCreatedAt(new java.util.Date());
                
                // Insert report
                reportDao.insert(report);
                
                // Update product status to "Khiếu nại"
                product.setStatus("Khiếu nại");
                productDao.update(product);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã gửi khiếu nại thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Show notification to buyer
                    showNotification("Khiếu nại đã được gửi", "Khiếu nại của bạn đã được gửi và đang chờ người bán xử lý.");
                    
                    // Create notification for seller
                    createNotification(product.getUserId(), "COMPLAINT_NEW", 
                        "Đơn hàng #" + order.getId() + " có khiếu nại mới từ người mua", "ORDER", (int) order.getId());
                    
                    // Set result to notify calling activity that data has changed
                    setResult(RESULT_OK);
                    
                    // Reload the display
                    loadOrderData();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tạo khiếu nại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showNotification(String title, String message) {
        // Create a custom notification dialog
        new android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("Đã hiểu", (dialog, which) -> dialog.dismiss())
            .setCancelable(false)
            .show();
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
                Log.d("OrderDetail", "Created notification successfully");
            } catch (Exception e) {
                Log.e("OrderDetail", "Error creating notification: " + e.getMessage());
            }
        });
    }
}
