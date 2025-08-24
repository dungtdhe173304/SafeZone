package com.group5.safezone.view.base;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.group5.safezone.R;
import com.group5.safezone.view.MainActivity;
//import com.group5.safezone.view.AuctionActivity;
//import com.group5.safezone.view.HomeActivity;
//import com.group5.safezone.view.ProductsActivity;
//import com.group5.safezone.view.ProfileActivity;
import com.group5.safezone.view.Wallet.WalletActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected LinearLayout footerHome, footerProducts, footerAuction, footerWallet, footerProfile;
    protected ImageView icHome, icProducts, icAuction, icWallet, icProfile;
    protected TextView tvHome, tvProducts, tvAuction, tvWallet, tvProfile;

    protected abstract int getCurrentPageIndex(); // 0: Home, 1: Products, 2: Auction, 3: Wallet, 4: Profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupFooter() {
        // Initialize footer views
        footerHome = findViewById(R.id.footer_home);
        footerProducts = findViewById(R.id.footer_products);
        footerAuction = findViewById(R.id.footer_auction);
        footerWallet = findViewById(R.id.footer_wallet);
        footerProfile = findViewById(R.id.footer_profile);

        icHome = findViewById(R.id.ic_home);
        icProducts = findViewById(R.id.ic_products);
        icAuction = findViewById(R.id.ic_auction);
        icWallet = findViewById(R.id.ic_wallet);
        icProfile = findViewById(R.id.ic_profile);

        tvHome = findViewById(R.id.tv_home);
        tvProducts = findViewById(R.id.tv_products);
        tvAuction = findViewById(R.id.tv_auction);
        tvWallet = findViewById(R.id.tv_wallet);
        tvProfile = findViewById(R.id.tv_profile);

        // Set click listeners
        footerHome.setOnClickListener(v -> navigateToPage(0));
        footerProducts.setOnClickListener(v -> navigateToPage(1));
        footerAuction.setOnClickListener(v -> navigateToPage(2));
        footerWallet.setOnClickListener(v -> navigateToPage(3));
        footerProfile.setOnClickListener(v -> navigateToPage(4));

        // Highlight current page
        updateFooterSelection();
    }

    private void navigateToPage(int pageIndex) {
        if (pageIndex == getCurrentPageIndex()) {
            return; // Đã ở trang hiện tại
        }

        Intent intent = null;
        switch (pageIndex) {
            case 0:
                intent = new Intent(this, MainActivity.class);
                break;
            case 1:
                // Products page - stay in MainActivity but load HomeFragment
                if (this instanceof MainActivity) {
                    ((MainActivity) this).loadHomeFragment();
                    return;
                }
                intent = new Intent(this, MainActivity.class);
                break;
            case 2:
                // Currently route Auction to Main for now
                intent = new Intent(this, MainActivity.class);
                break;
            case 3:
                intent = new Intent(this, WalletActivity.class);
                break;
            case 4:
               // intent = new Intent(this, ProfileActivity.class);
                break;
            default:
                return;
        }

        if (intent != null) {
            try {
                startActivity(intent);
                overridePendingTransition(0, 0); // Không có animation
                // Chỉ finish() khi chuyển về MainActivity
                if (pageIndex == 0) {
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Log lỗi để debug
                android.util.Log.e("BaseActivity", "Error navigating to page " + pageIndex, e);
            }
        }
    }

    private void updateFooterSelection() {
        // Reset all items
        resetFooterItem(icHome, tvHome, footerHome);
        resetFooterItem(icProducts, tvProducts, footerProducts);
        resetFooterItem(icAuction, tvAuction, footerAuction);
        resetFooterItem(icWallet, tvWallet, footerWallet);
        resetFooterItem(icProfile, tvProfile, footerProfile);

        // Highlight current item
        int currentPage = getCurrentPageIndex();
        switch (currentPage) {
            case 0:
                selectFooterItem(icHome, tvHome, footerHome);
                break;
            case 1:
                selectFooterItem(icProducts, tvProducts, footerProducts);
                break;
            case 2:
                selectFooterItem(icAuction, tvAuction, footerAuction);
                break;
            case 3:
                selectFooterItem(icWallet, tvWallet, footerWallet);
                break;
            case 4:
                selectFooterItem(icProfile, tvProfile, footerProfile);
                break;
        }
    }

    private void resetFooterItem(ImageView icon, TextView text, LinearLayout container) {
        icon.setColorFilter(ContextCompat.getColor(this, R.color.footer_icon_color));
        text.setTextColor(ContextCompat.getColor(this, R.color.footer_text_color));
        container.setBackgroundResource(0);
    }

    private void selectFooterItem(ImageView icon, TextView text, LinearLayout container) {
        icon.setColorFilter(ContextCompat.getColor(this, R.color.footer_icon_selected));
        text.setTextColor(ContextCompat.getColor(this, R.color.footer_text_selected));
        container.setBackgroundResource(R.drawable.footer_item_selected);
    }
}
