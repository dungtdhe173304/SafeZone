package com.group5.safezone.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.group5.safezone.R;
import com.group5.safezone.config.AuthInterceptor;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.view.AdminAuctionApprovalActivity;
import com.group5.safezone.view.admin.UserManagementActivity;
import com.group5.safezone.view.admin.ProductManagementActivity;
import com.group5.safezone.view.admin.OrderManagementActivity;
import com.group5.safezone.view.admin.ReportsActivity;
import com.group5.safezone.view.auth.LoginActivity;

public class AdminMainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvWelcome;
    private CardView cardUserManagement, cardProductManagement, cardOrderManagement, cardAuctionManagement, cardReports, cardSystemSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra quyền admin
        if (!AuthInterceptor.checkAdminPermission(this)) {
            return;
        }

        setContentView(R.layout.activity_admin_main);

        initViews();
        setupToolbar();
        setupClickListeners();

        sessionManager = new SessionManager(this);
        displayWelcomeMessage();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        cardUserManagement = findViewById(R.id.cardUserManagement);
        cardProductManagement = findViewById(R.id.cardProductManagement);
        cardOrderManagement = findViewById(R.id.cardOrderManagement);
        cardAuctionManagement = findViewById(R.id.cardAuctionManagement);
        cardReports = findViewById(R.id.cardReports);
        cardSystemSettings = findViewById(R.id.cardSystemSettings);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quản trị viên");
    }

    private void setupClickListeners() {
        cardUserManagement.setOnClickListener(v -> {
            startActivity(new Intent(this, UserManagementActivity.class));
        });

        cardProductManagement.setOnClickListener(v -> {
            startActivity(new Intent(this, ProductManagementActivity.class));
        });

        cardOrderManagement.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderManagementActivity.class));
        });

        cardAuctionManagement.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAuctionApprovalActivity.class));
        });

        cardReports.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
        });

        cardSystemSettings.setOnClickListener(v -> {
            // startActivity(new Intent(this, SystemSettingsActivity.class));
        });
    }

    private void displayWelcomeMessage() {
        String userName = sessionManager.getUserName();
        tvWelcome.setText("Chào mừng, " + userName + "!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
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

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra lại quyền khi quay lại activity
        AuthInterceptor.checkAdminPermission(this);
        AuthInterceptor.checkAccountStatus(this);
    }
}
