package com.group5.safezone.view.livestreaming;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

public class LiveStreamingActivity extends AppCompatActivity implements ShareDialog.OnShareListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_streaming);

        // Check permissions before starting live stream
        if (checkPermissions()) {
            addFragment();
        }
        setupShareButton();
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
            btnShare.setOnClickListener(v -> {
                showShareDialog();
            });
        }

        // Add camera and microphone status check buttons
        Button btnCheckCamera = findViewById(R.id.btn_check_camera);
        Button btnCheckMic = findViewById(R.id.btn_check_mic);
        
        if (btnCheckCamera != null) {
            btnCheckCamera.setOnClickListener(v -> {
                LiveStreamingHelper.showCameraStatus(this);
                showTroubleshootingDialog("Camera", LiveStreamingHelper.getCameraTroubleshootingTips());
            });
        }
        
        if (btnCheckMic != null) {
            btnCheckMic.setOnClickListener(v -> {
                LiveStreamingHelper.showMicrophoneStatus(this);
                showTroubleshootingDialog("Microphone", LiveStreamingHelper.getMicrophoneTroubleshootingTips());
            });
        }
    }

    private void updateStreamTitle() {
        TextView tvTitle = findViewById(R.id.tv_stream_title);
        if (tvTitle != null) {
            String title = isHost ? "🎥 Live Stream (Host)" : "👁️ Live Stream (Viewer)";
            tvTitle.setText(title);
        }
    }

    /**
     * Check if required permissions are granted
     */
    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Request permissions
                requestPermissions();
                return false;
            }
        }
        return true;
    }

    /**
     * Request required permissions
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    /**
     * Handle permission request result
     */
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
                // All permissions granted, start live stream
                addFragment();
                Toast.makeText(this, "Camera and microphone permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                // Some permissions denied
                Toast.makeText(this, "Camera and microphone permissions are required for live streaming!", Toast.LENGTH_LONG).show();
                // You can show a dialog explaining why permissions are needed
                showPermissionExplanationDialog();
            }
        }
    }

    /**
     * Show dialog explaining why permissions are needed
     */
    private void showPermissionExplanationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Camera and microphone permissions are required to start live streaming. Please grant these permissions in Settings.")
            .setPositiveButton("Grant Permissions", (dialog, which) -> {
                requestPermissions();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                finish(); // Close activity if permissions not granted
            })
            .setCancelable(false)
            .show();
    }

    /**
     * Show troubleshooting dialog for camera or microphone issues
     */
    private void showTroubleshootingDialog(String title, String tips) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title + " Troubleshooting")
            .setMessage(tips)
            .setPositiveButton("Got it", null)
            .setNeutralButton("Check Status", (dialog, which) -> {
                if ("Camera".equals(title)) {
                    LiveStreamingHelper.showCameraStatus(this);
                } else {
                    LiveStreamingHelper.showMicrophoneStatus(this);
                }
            })
            .show();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // Handle back button press for live streaming
        super.onBackPressed();
    }
}
