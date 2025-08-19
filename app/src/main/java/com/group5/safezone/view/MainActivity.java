package com.group5.safezone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.adapter.UserAdapter;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.config.AuthInterceptor;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.config.NotificationPermissionUtil;
import com.group5.safezone.config.NotificationEvents;
import com.group5.safezone.view.Wallet.WalletActivity;
import com.group5.safezone.view.admin.AdminMainActivity;
import com.group5.safezone.view.auth.LoginActivity;
import com.group5.safezone.view.base.BaseActivity;
import com.group5.safezone.view.notification.NotificationsActivity;
import com.group5.safezone.viewmodel.UserViewModel;
import com.google.android.material.navigation.NavigationView;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private UserViewModel userViewModel;
    private UserAdapter userAdapter;
    private SessionManager sessionManager;

    // UI Components
    private ProgressBar progressBar;
    private TextView tvError, tvWelcome;
    private CardView cardUserInfo;
    private TextView tvUserName, tvEmail, tvPhone, tvRole, tvBalance, tvStatus, tvVerify;
    private RecyclerView recyclerViewUsers;
    private Button btnLoadUser, btnLoadAllUsers;
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

        initViews();
        setupToolbar();
        setupDrawer();
        setupBackPressedDispatcher();
        // Hỏi quyền/thông báo nếu đang bị tắt ngay khi vào app
        NotificationPermissionUtil.promptIfNeeded(this, 7001);
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        setupFooter();

        displayWelcomeMessage();

        // Load dữ liệu ban đầu
        userViewModel.loadAllUsers();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvWelcome = findViewById(R.id.tvWelcome);
        cardUserInfo = findViewById(R.id.cardUserInfo);
        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvRole = findViewById(R.id.tvRole);
        tvBalance = findViewById(R.id.tvBalance);
        tvStatus = findViewById(R.id.tvStatus);
        tvVerify = findViewById(R.id.tvVerify);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        btnLoadUser = findViewById(R.id.btnLoadUser);
        btnLoadAllUsers = findViewById(R.id.btnLoadAllUsers);
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
                    // stay on home
                } else if (id == R.id.nav_wallet) {
                    startActivity(new Intent(this, WalletActivity.class));
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

    private void displayWelcomeMessage() {
        String userName = sessionManager.getUserName();
        String role = sessionManager.getUserRole();
        tvWelcome.setText("Chào mừng, " + userName + " (" + role + ")!");
    }

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

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter();
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(user -> {
            displayUserInfo(user);
            Toast.makeText(this, "Selected: " + user.getUserName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        btnLoadUser.setOnClickListener(v -> {
            // Load current user
            int currentUserId = sessionManager.getUserId();
            userViewModel.getUserById(currentUserId);
        });

        btnLoadAllUsers.setOnClickListener(v -> {
            userViewModel.loadAllUsers();
        });
    }

    private void observeViewModel() {
        // Observe loading state
        userViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        userViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                tvError.setText(errorMessage);
                tvError.setVisibility(View.VISIBLE);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            } else {
                tvError.setVisibility(View.GONE);
            }
        });

        // Observe current user
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                displayUserInfo(user);
            }
        });

        // Observe all users
        userViewModel.getAllUsers().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                userAdapter.setUsers(users);
                // Hiển thị user đầu tiên
                displayUserInfo(users.get(0));
            }
        });
    }

    private void displayUserInfo(User user) {
        cardUserInfo.setVisibility(View.VISIBLE);

        tvUserName.setText(user.getUserName() != null ? user.getUserName() : "N/A");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
        tvRole.setText(user.getRole() != null ? user.getRole() : "N/A");
        tvStatus.setText(user.getStatus() != null ? user.getStatus() : "N/A");

        // Format balance
        if (user.getBalance() != null) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvBalance.setText(formatter.format(user.getBalance()));
        } else {
            tvBalance.setText("0 VNĐ");
        }

        // Verify status
        tvVerify.setText(user.getIsVerify() != null && user.getIsVerify() ? "Đã xác thực" : "Chưa xác thực");
        tvVerify.setTextColor(user.getIsVerify() != null && user.getIsVerify() ?
                getResources().getColor(android.R.color.holo_green_dark) :
                getResources().getColor(android.R.color.holo_red_dark));
    }

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (notificationReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
            notificationReceiver = null;
        }
    }
}

