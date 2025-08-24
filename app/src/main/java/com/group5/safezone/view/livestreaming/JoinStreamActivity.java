package com.group5.safezone.view.livestreaming;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.group5.safezone.R;
import com.group5.safezone.config.SessionManager;

/**
 * Activity for joining live streams by entering Stream ID
 * Users can input a Stream ID shared by their friends to join as audience
 */
public class JoinStreamActivity extends AppCompatActivity {

    private EditText etStreamID;
    private Button btnJoinStream, btnScanQR, btnRecentStreams;
    private TextView tvInstructions, tvStreamInfo;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_stream);

        sessionManager = new SessionManager(this);
        initViews();
        setupToolbar();
        setupListeners();
        setDefaultValues();
    }

    private void initViews() {
        etStreamID = findViewById(R.id.et_stream_id);
        btnJoinStream = findViewById(R.id.btn_join_stream);
        btnScanQR = findViewById(R.id.btn_scan_qr);
        btnRecentStreams = findViewById(R.id.btn_recent_streams);
        tvInstructions = findViewById(R.id.tv_instructions);
        tvStreamInfo = findViewById(R.id.tv_stream_info);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Join Live Stream");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        btnJoinStream.setOnClickListener(v -> joinStream());
        
        btnScanQR.setOnClickListener(v -> {
            // TODO: Implement QR code scanning
            Toast.makeText(this, "QR Code scanning coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        btnRecentStreams.setOnClickListener(v -> {
            // TODO: Show recent streams list
            Toast.makeText(this, "Recent streams feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Real-time validation as user types
        etStreamID.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateStreamID(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setDefaultValues() {
        // Check if Stream ID was passed from intent (e.g., from share invitation)
        String streamID = getIntent().getStringExtra("streamID");
        if (streamID != null && !streamID.isEmpty()) {
            etStreamID.setText(streamID);
            validateStreamID(streamID);
        }
    }

    private void joinStream() {
        String streamID = etStreamID.getText().toString().trim();
        
        if (validateStreamID(streamID)) {
            // Get current user info
            String userID = "user_" + sessionManager.getUserId();
            String userName = sessionManager.getUserName();
            
            // Start live streaming as audience
            Intent intent = new Intent(this, LiveStreamingActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("userName", userName);
            intent.putExtra("host", false);
            intent.putExtra("liveID", streamID);
            
            startActivity(intent);
            finish(); // Close this activity
        }
    }

    private boolean validateStreamID(String streamID) {
        if (streamID.isEmpty()) {
            tvStreamInfo.setText("Enter a Stream ID to join");
            tvStreamInfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
            btnJoinStream.setEnabled(false);
            return false;
        }

        // Check if Stream ID contains only valid characters
        String pattern = "^[a-zA-Z0-9_]+$";
        if (!streamID.matches(pattern)) {
            tvStreamInfo.setText("❌ Stream ID can only contain letters, numbers, and underscores");
            tvStreamInfo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnJoinStream.setEnabled(false);
            return false;
        }

        // Stream ID looks valid
        tvStreamInfo.setText("✅ Valid Stream ID: " + streamID);
        tvStreamInfo.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        btnJoinStream.setEnabled(true);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
