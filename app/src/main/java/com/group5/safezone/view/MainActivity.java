package com.group5.safezone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
// import com.group5.safezone.adapter.UserAdapter;
import com.group5.safezone.adapter.AuctionItemAdapter;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.config.AuthInterceptor;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.view.admin.AdminMainActivity;
import com.group5.safezone.view.auth.LoginActivity;
import com.group5.safezone.view.base.BaseActivity;
import com.group5.safezone.viewmodel.UserViewModel;
import com.group5.safezone.viewmodel.AuctionViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private UserViewModel userViewModel;
    private AuctionViewModel auctionViewModel;
    private AuctionItemAdapter auctionAdapter;
    private SessionManager sessionManager;

    // UI Components
    private ProgressBar progressBar;
    private TextView tvError, tvWelcome, tvHelloUser, tvBalancePill;
    private ImageView ivAvatar;
    private RecyclerView recyclerViewAuctions;
    private TextView etSearch; // using TextView for simplicity (EditText id)
    private Button btnSearch;

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
        observeAuctionViewModel();
        setupFooter();

        displayWelcomeMessage();

        // Load dữ liệu ban đầu
        int uid = sessionManager.getUserId();
        if (uid > 0) {
            userViewModel.getUserById(uid);
        }
        auctionViewModel.loadAuctions(uid);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvWelcome = null; // header common
        tvHelloUser = findViewById(R.id.tvHelloUser);
        tvBalancePill = findViewById(R.id.tvBalancePill);
        ivAvatar = findViewById(R.id.ivAvatar);
        recyclerViewAuctions = findViewById(R.id.recyclerViewAuctions);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SafeZone");
    }

    private void displayWelcomeMessage() {
        String userName = sessionManager.getUserName();
        String role = sessionManager.getUserRole();
        tvHelloUser.setText("Chào mừng, " + (userName != null ? userName : "Khách") + "!");
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi","VN"));
        String balanceText = formatter.format(sessionManager.getBalance());
        tvBalancePill.setText(balanceText);
        ivAvatar.setImageResource(R.drawable.ic_person);
    }

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        auctionViewModel = new ViewModelProvider(this).get(AuctionViewModel.class);
    }

    private void setupRecyclerView() {
        auctionAdapter = new AuctionItemAdapter(this);
        recyclerViewAuctions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAuctions.setAdapter(auctionAdapter);

        auctionAdapter.setActionListener(new AuctionItemAdapter.OnAuctionActionListener() {
            @Override
            public void onRegisterClick(com.group5.safezone.model.ui.AuctionItemUiModel item) {
                if (sessionManager.getUserId() <= 0) {
                    Toast.makeText(MainActivity.this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }
                android.content.Intent intent = new android.content.Intent(MainActivity.this, RegisterAuctionActivity.class);
                intent.putExtra(RegisterAuctionActivity.EXTRA_AUCTION_ID, item.getAuction().getId());
                startActivity(intent);
            }

            @Override
            public void onEnterRoomClick(com.group5.safezone.model.ui.AuctionItemUiModel item) {
                Toast.makeText(MainActivity.this, "Vào phòng đấu giá (đang phát triển)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnSearch.setOnClickListener(v -> {
            int uid = sessionManager.getUserId();
            String keyword = etSearch.getText() != null ? etSearch.getText().toString() : "";
            auctionViewModel.searchAuctions(uid, keyword);
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

        // Observe current user (cập nhật số dư pill)
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi","VN"));
                String balanceText = formatter.format(user.getBalance() != null ? user.getBalance() : 0);
                tvBalancePill.setText(balanceText);
                tvHelloUser.setText("Chào mừng, " + (user.getUserName() != null ? user.getUserName() : "Khách") + "!");
            }
        });

        // No users list on home
    }

    private void observeAuctionViewModel() {
        auctionViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        auctionViewModel.getItems().observe(this, items -> {
            if (items != null) {
                auctionAdapter.submitList(items);
            }
        });

        auctionViewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                tvError.setText(msg);
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setVisibility(View.GONE);
            }
        });
    }

    // Bỏ hiển thị thông tin chi tiết người dùng

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

