package com.group5.safezone.view.livestreaming;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.group5.safezone.R;

/**
 * Demo activity to demonstrate live streaming functionality
 * Shows how to start a live stream as host or join as audience
 */
public class LiveStreamingDemoActivity extends AppCompatActivity {

    private EditText etUserID, etUserName, etLiveID;
    private Button btnStartAsHost, btnJoinAsAudience, btnShareDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_streaming_demo);

        initViews();
        setupListeners();
        setDefaultValues();
    }

    private void initViews() {
        etUserID = findViewById(R.id.et_user_id);
        etUserName = findViewById(R.id.et_user_name);
        etLiveID = findViewById(R.id.et_live_id);
        btnStartAsHost = findViewById(R.id.btn_start_as_host);
        btnJoinAsAudience = findViewById(R.id.btn_join_as_audience);
        btnShareDemo = findViewById(R.id.btn_share_demo);
    }

    private void setupListeners() {
        btnStartAsHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLiveStreamAsHost();
            }
        });

        btnJoinAsAudience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open JoinStreamActivity instead of direct join
                Intent intent = new Intent(LiveStreamingDemoActivity.this, JoinStreamActivity.class);
                startActivity(intent);
            }
        });

        btnShareDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareDemoStream();
            }
        });
    }

    private void setDefaultValues() {
        // Set default values for testing
        etUserID.setText("user_" + System.currentTimeMillis());
        etUserName.setText("User_" + System.currentTimeMillis());
        etLiveID.setText("live_" + System.currentTimeMillis());
    }

    private void startLiveStreamAsHost() {
        String userID = etUserID.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();
        String liveID = etLiveID.getText().toString().trim();

        if (validateInputs(userID, userName, liveID)) {
            LiveStreamingManager.startLiveStreamAsHost(this, userID, userName, liveID);
        }
    }

    private void joinLiveStreamAsAudience() {
        String userID = etUserID.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();
        String liveID = etLiveID.getText().toString().trim();

        if (validateInputs(userID, userName, liveID)) {
            LiveStreamingManager.joinLiveStreamAsAudience(this, userID, userName, liveID);
        }
    }

    private boolean validateInputs(String userID, String userName, String liveID) {
        if (userID.isEmpty()) {
            Toast.makeText(this, "Please enter User ID", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter User Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (liveID.isEmpty()) {
            Toast.makeText(this, "Please enter Live ID", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate that inputs only contain numbers, letters, and underscores
        String pattern = "^[a-zA-Z0-9_]+$";
        if (!userID.matches(pattern) || !userName.matches(pattern) || !liveID.matches(pattern)) {
            Toast.makeText(this, "User ID, User Name, and Live ID can only contain numbers, letters, and underscores (_)", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void shareDemoStream() {
        String userID = etUserID.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();
        String liveID = etLiveID.getText().toString().trim();

        if (validateInputs(userID, userName, liveID)) {
            String streamTitle = "Demo Live Stream by " + userName;
            ShareManager.shareLiveStreamInvitation(this, liveID, streamTitle, userName);
        }
    }
}
