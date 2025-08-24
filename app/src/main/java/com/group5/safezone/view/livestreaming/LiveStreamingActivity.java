package com.group5.safezone.view.livestreaming;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.group5.safezone.R;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment;

import android.util.Log;

public class LiveStreamingActivity extends AppCompatActivity implements ShareDialog.OnShareListener, DonateDialog.OnDonateListener {

    // ZEGOCLOUD credentials from documentation
    private static final long APP_ID = 306600199L;
    private static final String APP_SIGN = "320f9747bd7cfc0c891f592df9166e7bc611968b8ed9fbc9ff43909f216036fa";
    
    // Permission constants
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    };
    
    // Stream information
    private String userID;
    private String userName;
    private String liveID;
    private boolean isHost;
    
    // Donate system
    private DonateManager donateManager;
    private DonateAnimationView donateAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_streaming);

        // Initialize donate system
        donateManager = new DonateManager(this);
        donateAnimationView = findViewById(R.id.donate_animation_view);

        // Check permissions before starting live stream
        if (checkPermissions()) {
            addFragment();
        }
        setupShareButton();
        setupDonateButton();
        updateStreamTitle();
    }

    public void addFragment() {
        // Get user information from intent
        userID = getIntent().getStringExtra("userID");
        userName = getIntent().getStringExtra("userName");
        isHost = getIntent().getBooleanExtra("host", false);
        liveID = getIntent().getStringExtra("liveID");

        // Set default values if not provided
        if (userID == null) userID = "user_" + System.currentTimeMillis();
        if (userName == null) userName = "User_" + System.currentTimeMillis();
        if (liveID == null) liveID = "live_" + System.currentTimeMillis();

        // Configure live streaming based on role with proper camera and audio settings
        ZegoUIKitPrebuiltLiveStreamingConfig config;
        if (isHost) {
            config = ZegoUIKitPrebuiltLiveStreamingConfig.host();
            
            // Enable camera and audio for host
            // Note: ZEGOCLOUD UI Kit handles camera and microphone automatically
            // The host will see camera controls in the UI to turn on/off camera and mic
            
        } else {
            config = ZegoUIKitPrebuiltLiveStreamingConfig.audience();
            
            // Audience configuration - they can join and watch the stream
            // Camera and microphone are typically disabled for audience by default
        }

        // Create and add the live streaming fragment
        ZegoUIKitPrebuiltLiveStreamingFragment fragment = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
            APP_ID, APP_SIGN, userID, userName, liveID, config);
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitNow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show share button for hosts
        if (isHost) {
            getMenuInflater().inflate(R.menu.menu_live_streaming, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_share) {
            showShareDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showShareDialog() {
        String streamTitle = "Live Stream by " + userName;
        ShareDialog dialog = ShareDialog.newInstance(liveID, streamTitle, userName);
        dialog.setOnShareListener(this);
        dialog.show(getSupportFragmentManager(), "ShareDialog");
    }

    @Override
    public void onShare(String platform, String customMessage) {
        String streamTitle = "Live Stream by " + userName;
        
        if ("General Share".equals(platform)) {
            ShareManager.shareLiveStreamInvitation(this, liveID, streamTitle, userName);
        } else {
            ShareManager.shareToSpecificPlatform(this, liveID, streamTitle, userName, platform);
        }
        
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            ShareManager.shareWithCustomMessage(this, liveID, streamTitle, userName, customMessage);
        }
        
        Toast.makeText(this, "Sharing live stream invitation...", Toast.LENGTH_SHORT).show();
    }

    private void setupShareButton() {
        Button btnShare = findViewById(R.id.btn_share_stream);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> showShareDialog());
        }
    }

    private void setupDonateButton() {
        Button btnDonate = findViewById(R.id.btn_donate);
        if (btnDonate != null) {
            // Chỉ hiển thị nút donate cho audience (người xem)
            if (!isHost) {
                btnDonate.setVisibility(View.VISIBLE);
                btnDonate.setOnClickListener(v -> showDonateDialog());
            } else {
                btnDonate.setVisibility(View.GONE);
            }
        }
    }

    private void showDonateDialog() {
        try {
            // Lấy host ID từ userID hiện tại (trong trường hợp này, host là người đang stream)
            // userID có thể là "user_3", chúng ta cần lấy số thực
            String hostId = userID;
            String hostName = userName;
            
            if (hostId == null || hostName == null || liveID == null) {
                Toast.makeText(this, "Lỗi: Thông tin livestream không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Log để debug
            Log.d("LiveStreaming", "Showing donate dialog - hostId: " + hostId + ", hostName: " + hostName + ", liveID: " + liveID);
            
            DonateDialog dialog = DonateDialog.newInstance(hostId, hostName, liveID);
            if (dialog != null) {
                dialog.setOnDonateListener(this);
                dialog.show(getSupportFragmentManager(), "DonateDialog");
            } else {
                Toast.makeText(this, "Lỗi: Không thể tạo dialog donate", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("LiveStreaming", "Error showing donate dialog: " + e.getMessage());
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDonate(int amount, String hostId, String liveId) {
        // Xử lý donate
        donateManager.processDonate(amount, hostId, liveId, new DonateManager.OnDonateResultListener() {
            @Override
            public void onDonateSuccess(int donatedAmount, double hostReceivedAmount) {
                // Hiển thị animation donate
                showDonateAnimation(donatedAmount);
                
                // Có thể thêm notification cho host
                Toast.makeText(LiveStreamingActivity.this, 
                    "Cảm ơn bạn đã donate " + formatCurrency(donatedAmount) + "!", 
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDonateFailed(String errorMessage) {
                Toast.makeText(LiveStreamingActivity.this, 
                    "Donate thất bại: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDonateAnimation(int amount) {
        if (donateAnimationView != null) {
            // Chọn emoji dựa trên số tiền
            String emoji = getEmojiForAmount(amount);
            
            donateAnimationView.startDonateAnimation(amount, emoji, () -> {
                // Animation hoàn thành
                Toast.makeText(this, "Animation donate hoàn thành!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private String getEmojiForAmount(int amount) {
        if (amount >= 100000) return "👑";
        if (amount >= 50000) return "💎";
        if (amount >= 20000) return "🌟";
        if (amount >= 10000) return "🎉";
        return "💖";
    }

    private String formatCurrency(int amount) {
        if (amount >= 1000) {
            return String.format("%.0fK VNĐ", amount / 1000.0);
        }
        return amount + " VNĐ";
    }

    private void updateStreamTitle() {
        TextView tvStreamTitle = findViewById(R.id.tv_stream_title);
        if (tvStreamTitle != null) {
            String title = isHost ? "Live Stream - " + userName : "Watching - " + userName;
            tvStreamTitle.setText(title);
        }
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                addFragment();
            } else {
                Toast.makeText(this, "Camera and microphone permissions are required for live streaming", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
