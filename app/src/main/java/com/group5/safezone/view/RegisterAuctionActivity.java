package com.group5.safezone.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.group5.safezone.R;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.view.base.BaseActivity;
import com.group5.safezone.viewmodel.AuctionViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class RegisterAuctionActivity extends BaseActivity {
    public static final String EXTRA_AUCTION_ID = "extra_auction_id";
    private AuctionViewModel viewModel;
    private SessionManager sessionManager;

    private ImageView ivThumb;
    private TextView tvName, tvStartPrice, tvCurrentPrice, tvBalance, tvWarning;
    private Button btnConfirm;
    private android.widget.ImageButton btnBack;
    private TextView headerHello, headerBalance;
    private ImageView headerAvatar;
    private double depositAmount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_auction);
        // Initialize footer navigation
        setupFooter();

        viewModel = new ViewModelProvider(this).get(AuctionViewModel.class);
        sessionManager = new SessionManager(this);

        ivThumb = findViewById(R.id.ivThumb);
        tvName = findViewById(R.id.tvName);
        tvStartPrice = findViewById(R.id.tvStartPrice);
        tvCurrentPrice = findViewById(R.id.tvCurrentPrice);
        tvBalance = findViewById(R.id.tvBalance);
        tvWarning = findViewById(R.id.tvWarning);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Header shared views
        headerHello = findViewById(R.id.tvHelloUser);
        headerBalance = findViewById(R.id.tvBalancePill);
        headerAvatar = findViewById(R.id.ivAvatar);
        headerAvatar.setImageResource(R.drawable.ic_person);
        headerHello.setText("Chào mừng, " + (sessionManager.getUserName() != null ? sessionManager.getUserName() : "Khách") + "!");
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
        headerBalance.setText(money.format(sessionManager.getBalance()));
        btnBack.setVisibility(View.VISIBLE);

        int auctionId = getIntent().getIntExtra(EXTRA_AUCTION_ID, -1);
        int uid = sessionManager.getUserId();
        viewModel.loadItem(auctionId, uid);

        NumberFormat moneyLocal = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
        tvBalance.setText("Số dư: " + moneyLocal.format(sessionManager.getBalance()));

        viewModel.getCurrentItem().observe(this, item -> {
            if (item == null) return;
            ivThumb.setImageResource(R.drawable.ic_inventory);
            tvName.setText(item.getProduct() != null ? item.getProduct().getProductName() : "Vật phẩm");
            Double start = item.getAuction().getStartPrice();
            Double current = item.getAuction().getCurrentHighestBid() != null && item.getAuction().getCurrentHighestBid() > 0
                    ? item.getAuction().getCurrentHighestBid()
                    : start;
            depositAmount = start != null ? start : 0;
            tvStartPrice.setText("Tiền đặt cọc: " + moneyLocal.format(depositAmount));
            tvCurrentPrice.setText("Giá cao nhất hiện tại: " + moneyLocal.format(current != null ? current : 0));
        });

        btnConfirm.setOnClickListener(v -> {
            boolean ok = viewModel.registerIfEnough(auctionId, sessionManager.getUserId());
            if (!ok) {
                Toast.makeText(this, "Số dư trong ví không đủ để đặt cọc", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                // Update session header balance locally
                double newBalance = sessionManager.getBalance() - depositAmount;
                if (newBalance < 0) newBalance = 0;
                sessionManager.updateBalance(newBalance);
                headerBalance.setText(NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(newBalance));
                finish();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected int getCurrentPageIndex() {
        return 2; // highlight Auction tab
    }
}


