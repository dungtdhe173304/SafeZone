package com.group5.safezone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.group5.safezone.R;
import com.group5.safezone.config.AuthInterceptor;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.config.NotificationPermissionUtil;
import com.group5.safezone.config.NotificationEvents;
import com.group5.safezone.view.Wallet.WalletActivity;
import com.group5.safezone.view.admin.AdminMainActivity;
import com.group5.safezone.view.auth.LoginActivity;
import com.group5.safezone.view.base.BaseActivity;
import com.group5.safezone.view.notification.NotificationsActivity;
import com.group5.safezone.view.PurchaseHistoryActivity;
import com.group5.safezone.view.SalesHistoryActivity;
import com.group5.safezone.view.component.CommunityChatHeaderView;
import com.group5.safezone.service.CommunityChatService;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.ZegoCloudConfig;
import com.group5.safezone.model.entity.ChatCommunity;
import com.group5.safezone.model.entity.User;
import com.google.android.material.navigation.NavigationView;
import com.group5.safezone.Constant.Chatbox.ConstantKey;

import com.zegocloud.zimkit.BuildConfig;
import com.zegocloud.zimkit.services.ZIMKit;
import java.util.List;
import java.util.Date;

public class MainActivity extends BaseActivity {

    private SessionManager sessionManager;

    // UI Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View notificationBellView;
    private TextView notificationBadge;
    private BroadcastReceiver notificationReceiver;
    
    // Community Chat Components
    private CommunityChatHeaderView communityChatHeaderView;
    private CommunityChatService communityChatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra authentication
        if (!AuthInterceptor.checkAuthentication(this)) {
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);

        // Initialize ZEGOCLOUD SDK safely
        ZegoCloudConfig.initSafely(this);

        initViews();
        setupToolbar();
        setupDrawer();
        setupBackPressedDispatcher();
        
        // Đảm bảo views đã được khởi tạo trước khi setup community chat
        new android.os.Handler().post(() -> setupCommunityChat());
        
        // Load HomeFragment as default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
        
        // Hỏi quyền/thông báo nếu đang bị tắt ngay khi vào app
        NotificationPermissionUtil.promptIfNeeded(this, 7001);
        
        // Database reset functionality removed - no longer needed
        
        // Setup footer for navigation
        setupFooter();
    }

    private void initViews() {
        System.out.println("=== MainActivity: initViews() called ===");
        
        drawerLayout = findViewById(R.id.drawer_layout);
        System.out.println("=== MainActivity: drawerLayout: " + (drawerLayout != null ? "NOT NULL" : "NULL") + " ===");
        
        navigationView = findViewById(R.id.navigation_view);
        System.out.println("=== MainActivity: navigationView: " + (navigationView != null ? "NOT NULL" : "NULL") + " ===");
        
        communityChatHeaderView = findViewById(R.id.community_chat_header);
        System.out.println("=== MainActivity: communityChatHeaderView: " + (communityChatHeaderView != null ? "NOT NULL" : "NULL") + " ===");
        
        if (communityChatHeaderView == null) {
            System.out.println("=== MainActivity: ERROR - community_chat_header not found in layout! ===");
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SafeZone");
    }
    
    private void setupCommunityChat() {
        System.out.println("=== MainActivity: setupCommunityChat() called ===");
        android.util.Log.d("MainActivity", "setupCommunityChat() called");
        
        if (communityChatHeaderView != null) {
            System.out.println("=== MainActivity: communityChatHeaderView is NOT NULL ===");
            android.util.Log.d("MainActivity", "communityChatHeaderView is NOT NULL");
            
            try {
                AppDatabase database = AppDatabase.getDatabase(this);
                System.out.println("=== MainActivity: Database initialized successfully ===");
                
                communityChatService = new CommunityChatService(database, sessionManager);
                System.out.println("=== MainActivity: CommunityChatService created successfully ===");
                
                communityChatHeaderView.setChatService(communityChatService);
                System.out.println("=== MainActivity: ChatService set to header view ===");
                android.util.Log.d("MainActivity", "ChatService set to header view");
                
                // Đảm bảo ChatService đã được set trước khi load messages
                android.util.Log.d("MainActivity", "About to load recent messages...");
                
                // Load recent messages
                new Thread(() -> {
                    try {
                        System.out.println("=== MainActivity: Loading recent messages... ===");
                        List<ChatCommunity> recentMessages = communityChatService.getRecentMessages(10);
                        System.out.println("=== MainActivity: Got " + (recentMessages != null ? recentMessages.size() : 0) + " messages ===");
                        
                        runOnUiThread(() -> {
                            if (recentMessages != null && !recentMessages.isEmpty()) {
                                System.out.println("=== MainActivity: Setting " + recentMessages.size() + " messages to header ===");
                                communityChatHeaderView.setMessages(recentMessages);
                            } else {
                                System.out.println("=== MainActivity: No messages found, creating sample message ===");
                                // Nếu không có tin nhắn, tạo tin nhắn mẫu để test
                                createSampleMessages();
                            }
                        });
                    } catch (Exception e) {
                        System.out.println("=== MainActivity: Error loading messages: " + e.getMessage() + " ===");
                        e.printStackTrace();
                        // Tạo tin nhắn mẫu nếu có lỗi
                        runOnUiThread(this::createSampleMessages);
                    }
                }).start();
                
            } catch (Exception e) {
                System.out.println("=== MainActivity: Error in setupCommunityChat: " + e.getMessage() + " ===");
                e.printStackTrace();
            }
        } else {
            System.out.println("=== MainActivity: communityChatHeaderView is NULL ===");
        }
    }
    
    private void createSampleMessages() {
        // Tạo tin nhắn mẫu để test
        new Thread(() -> {
            try {
                AppDatabase database = AppDatabase.getDatabase(this);
                User currentUser = database.userDao().getUserById(sessionManager.getUserId());
                
                if (currentUser != null) {
                    // Tạo tin nhắn mẫu
                    ChatCommunity sampleMessage = new ChatCommunity();
                    sampleMessage.setMessage("Chào mừng bạn đến với SafeZone! Đây là tin nhắn cộng đồng đầu tiên.");
                    sampleMessage.setUserId(currentUser.getId());
                    sampleMessage.setUserName(currentUser.getUserName());
                    sampleMessage.setCreatedAt(new Date());
                    sampleMessage.setUpdatedAt(new Date());
                    sampleMessage.setDisplayed(false);
                    sampleMessage.setDisplayOrder(0);
                    
                    database.chatCommunityDao().insert(sampleMessage);
                    
                    // Reload messages
                    List<ChatCommunity> recentMessages = communityChatService.getRecentMessages(10);
                    runOnUiThread(() -> {
                        if (recentMessages != null && !recentMessages.isEmpty()) {
                            communityChatHeaderView.setMessages(recentMessages);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    // Load HomeFragment
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                } else if (id == R.id.nav_products) {
                    // Load HomeFragment (which contains products)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                } else if (id == R.id.nav_wallet) {
                    startActivity(new Intent(this, WalletActivity.class));
                } else if (id == R.id.nav_purchase_history) {
                    startActivity(new Intent(this, PurchaseHistoryActivity.class));
                } else if (id == R.id.nav_sales_history) {
                    startActivity(new Intent(this, SalesHistoryActivity.class));
                } else if (id == R.id.nav_chat) {
                    startActivity(new Intent(this, com.group5.safezone.view.chat.ChatActivity.class));
                } else if (id == R.id.nav_community_chat) {
                    startActivity(new Intent(this, CommunityChatActivity.class));
                } else if (id == R.id.nav_livestream) {
                    startActivity(new Intent(this, com.group5.safezone.view.livestreaming.LiveStreamingMainActivity.class));
                } else if (id == R.id.nav_settings) {
                    Toast.makeText(this, "Cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_about) {
                    Toast.makeText(this, "SafeZone v1.0", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_logout) {
                    logout();
                }
                drawerLayout.closeDrawers();
                return true;
            });

            // Bind header data
            View header = navigationView.getHeaderView(0);
            if (header != null) {
                TextView name = header.findViewById(R.id.tv_header_name);
                TextView email = header.findViewById(R.id.tv_header_email);
                if (name != null) name.setText(sessionManager.getUserName());
                if (email != null) email.setText(sessionManager.getEmail());

                // Add bell to header area of the main Toolbar instead of drawer header
                notificationBellView = getLayoutInflater().inflate(R.layout.action_notification, toolbar, false);
                Toolbar.LayoutParams lp = new Toolbar.LayoutParams(
                        Toolbar.LayoutParams.WRAP_CONTENT,
                        Toolbar.LayoutParams.MATCH_PARENT);
                lp.gravity = android.view.Gravity.END;
                notificationBellView.setLayoutParams(lp);
                toolbar.addView(notificationBellView);
                notificationBadge = notificationBellView.findViewById(R.id.tvBadge);
                notificationBellView.setOnClickListener(v -> {
                    startActivity(new Intent(this, NotificationsActivity.class));
                });
                refreshUnreadCount();
            }
        }
    }

    private void setupBackPressedDispatcher() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
                } else {
                    setEnabled(false);
                    MainActivity.super.onBackPressed();
                }
            }
        });
    }

    // Welcome message functionality removed - no longer needed in new layout

    private void refreshUnreadCount() {
        if (notificationBadge == null) return;
        new Thread(() -> {
            try {
                com.group5.safezone.repository.NotificationRepository nr = new com.group5.safezone.repository.NotificationRepository(getApplication());
                int count = nr.getUnreadByUserIdAsync(sessionManager.getUserId()).get().size();
                runOnUiThread(() -> {
                    if (count > 0) {
                        notificationBadge.setText(String.valueOf(Math.min(count, 99)));
                        notificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        notificationBadge.setVisibility(View.GONE);
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }

    // Live streaming functionality
    private void setupLiveStreaming() {
        // Start live stream as host
        String userID = "user_" + sessionManager.getUserId();
        String userName = sessionManager.getUserName();
        String liveID = "live_" + System.currentTimeMillis();
        
        Intent intent = new Intent(this, com.group5.safezone.view.livestreaming.LiveStreamingActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("userName", userName);
        intent.putExtra("host", true);
        intent.putExtra("liveID", liveID);
        startActivity(intent);
    }

    private void joinLiveStream() {
        // Open JoinStreamActivity to input Stream ID
        Intent intent = new Intent(this, com.group5.safezone.view.livestreaming.JoinStreamActivity.class);
        startActivity(intent);
    }

    // User info display methods removed - no longer needed in new layout

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Ẩn menu admin nếu không phải admin
        MenuItem adminItem = menu.findItem(R.id.action_admin);
        if (adminItem != null) {
            adminItem.setVisible(sessionManager.isAdmin());
        }

        // no toolbar bell (moved to header)

        return true;
    }

    private void refreshNotificationBadge(TextView badge) { /* kept for potential reuse */ }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_admin) {
            if (sessionManager.isAdmin()) {
                startActivity(new Intent(this, AdminMainActivity.class));
            }
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Back press handled via OnBackPressedDispatcher

    @Override
    protected int getCurrentPageIndex() {
        return 0; // Home page
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra lại authentication và status khi quay lại activity
        if (!AuthInterceptor.checkAuthentication(this)) {
            return;
        }
        AuthInterceptor.checkAccountStatus(this);

        // Lắng nghe cập nhật thông báo để cập nhật badge real-time
        if (notificationReceiver == null) {
            notificationReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (NotificationEvents.ACTION_UPDATED.equals(intent.getAction())) {
                        refreshUnreadCount();
                    }
                }
            };
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(notificationReceiver, new IntentFilter(NotificationEvents.ACTION_UPDATED));
        }

        // Cập nhật lại badge khi quay lại màn hình
        refreshUnreadCount();
        
        // Reload HomeFragment if it's the current fragment to refresh product list
        reloadCurrentFragment();
        
        // Refresh community chat header khi quay lại MainActivity
        if (communityChatHeaderView != null) {
            refreshCommunityChatHeader();
        }
    }
    
    private void reloadCurrentFragment() {
        // Get current fragment
        androidx.fragment.app.Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        
        if (currentFragment instanceof HomeFragment) {
            System.out.println("=== MainActivity: Reloading HomeFragment on resume ===");
            ((HomeFragment) currentFragment).reloadProducts();
        }
    }
    
    private void refreshCommunityChatHeader() {
        android.util.Log.d("MainActivity", "refreshCommunityChatHeader called");
        
        if (communityChatService != null) {
            android.util.Log.d("MainActivity", "communityChatService is not null, getting recent messages");
            
            // Lấy tin nhắn mới nhất và cập nhật header
            new Thread(() -> {
                try {
                    List<ChatCommunity> recentMessages = communityChatService.getRecentMessages(50);
                    android.util.Log.d("MainActivity", "Retrieved " + (recentMessages != null ? recentMessages.size() : 0) + " messages");
                    
                    if (recentMessages != null && !recentMessages.isEmpty()) {
                        runOnUiThread(() -> {
                            android.util.Log.d("MainActivity", "Setting messages to header view");
                            communityChatHeaderView.setMessages(recentMessages);
                        });
                    } else {
                        android.util.Log.d("MainActivity", "No messages to display");
                    }
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Error refreshing chat header", e);
                }
            }).start();
        } else {
            android.util.Log.d("MainActivity", "communityChatService is null");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (notificationReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
            notificationReceiver = null;
        }
    }


    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (communityChatService != null) {
            communityChatService.shutdown();
        }
    }

    public void loadHomeFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    // Database reset functionality removed - no longer needed
}

