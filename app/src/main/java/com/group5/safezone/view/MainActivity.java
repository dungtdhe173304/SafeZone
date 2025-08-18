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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import com.group5.safezone.view.admin.AdminMainActivity;
import com.group5.safezone.view.auth.LoginActivity;
import com.group5.safezone.view.base.BaseActivity;
import com.group5.safezone.viewmodel.UserViewModel;

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

    private void displayWelcomeMessage() {
        String userName = sessionManager.getUserName();
        String role = sessionManager.getUserRole();
        tvWelcome.setText("Chào mừng, " + userName + " (" + role + ")!");
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

        return true;
    }

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
    }
}

