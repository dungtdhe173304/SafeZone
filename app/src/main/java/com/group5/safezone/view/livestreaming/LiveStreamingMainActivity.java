package com.group5.safezone.view.livestreaming;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.group5.safezone.R;
import com.group5.safezone.config.SessionManager;

/**
 * Main Activity for Live Streaming features
 * Provides options to start a new stream or join existing streams
 */
public class LiveStreamingMainActivity extends AppCompatActivity {

    private Button btnStartStream, btnJoinStream, btnMyStreams;
    private TextView tvWelcome, tvInstructions;
    private CardView cardStartStream, cardJoinStream, cardMyStreams;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_streaming_main);

        sessionManager = new SessionManager(this);
        initViews();
        setupToolbar();
        setupListeners();
        displayUserInfo();
    }

    private void initViews() {
        btnStartStream = findViewById(R.id.btn_start_stream);
        btnJoinStream = findViewById(R.id.btn_join_stream);
        btnMyStreams = findViewById(R.id.btn_my_streams);
        tvWelcome = findViewById(R.id.tv_welcome);
        tvInstructions = findViewById(R.id.tv_instructions);
        cardStartStream = findViewById(R.id.card_start_stream);
        cardJoinStream = findViewById(R.id.card_join_stream);
        cardMyStreams = findViewById(R.id.card_my_streams);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.livestream_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        btnStartStream.setOnClickListener(v -> startNewStream());
        btnJoinStream.setOnClickListener(v -> joinExistingStream());
        btnMyStreams.setOnClickListener(v -> showMyStreams());
    }

    private void displayUserInfo() {
        String userName = sessionManager.getUserName();
        String role = sessionManager.getUserRole();
        tvWelcome.setText(getString(R.string.livestream_welcome) + " " + userName + "!");
        
        // Show/hide start stream option based on user role
        if ("admin".equalsIgnoreCase(role) || "moderator".equalsIgnoreCase(role)) {
            cardStartStream.setVisibility(View.VISIBLE);
            tvInstructions.setText(getString(R.string.livestream_user_instructions));
        } else {
            cardStartStream.setVisibility(View.GONE);
            tvInstructions.setText(getString(R.string.livestream_guest_instructions));
        }
    }

    private void startNewStream() {
        Intent intent = new Intent(this, LiveStreamingActivity.class);
        intent.putExtra("userID", "user_" + sessionManager.getUserId());
        intent.putExtra("userName", sessionManager.getUserName());
        intent.putExtra("host", true);
        startActivity(intent);
    }

    private void joinExistingStream() {
        Intent intent = new Intent(this, JoinStreamActivity.class);
        startActivity(intent);
    }

    private void showMyStreams() {
        // TODO: Implement my streams feature
        // This could show a list of streams created by the current user
        // or streams they've recently joined
        Intent intent = new Intent(this, LiveStreamingDemoActivity.class);
        startActivity(intent);
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
