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
import com.google.android.material.navigation.NavigationView;
import com.group5.safezone.Constant.Chatbox.ConstantKey;

import com.zegocloud.zimkit.BuildConfig;
import com.zegocloud.zimkit.services.ZIMKit;

public class MainActivity extends BaseActivity {

    private SessionManager sessionManager;

    // UI Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View notificationBellView;
    private TextView notificationBadge;
    private BroadcastReceiver notificationReceiver;

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

        // Initialize ZEGOCLOUD SDK
        initZegoCloud();

        initViews();
        setupToolbar();
        setupDrawer();
        setupBackPressedDispatcher();
        
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
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SafeZone");
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

    @Override
    protected void onPause() {
        super.onPause();
        if (notificationReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
            notificationReceiver = null;
        }
    }

    private void initZegoCloud() {
        ZIMKit.initWith(this.getApplication(), ConstantKey.appID, ConstantKey.appSign);
        ZIMKit.initNotifications();
    }

    public void loadHomeFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    // Database reset functionality removed - no longer needed
}

