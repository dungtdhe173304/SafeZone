package com.group5.safezone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.group5.safezone.R;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.ChatCommunity;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.service.CommunityChatService;
import com.group5.safezone.view.Wallet.WalletActivity;
import com.group5.safezone.adapter.CommunityChatAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommunityChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;
    private TextView balanceText;
    private TextView messageCostText;
    
    private AppDatabase database;
    private SessionManager sessionManager;
    private CommunityChatService chatService;
    private CommunityChatAdapter adapter;
    private List<ChatCommunity> messages;
    private User currentUser;
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat);
        
        initViews();
        setupToolbar();
        initDatabase();
        loadCurrentUser();
        setupRecyclerView();
        setupMessageInput();
        loadMessages();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        balanceText = findViewById(R.id.balance_text);
        messageCostText = findViewById(R.id.message_cost_text);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.community_chat_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void initDatabase() {
        database = AppDatabase.getDatabase(this);
        sessionManager = new SessionManager(this);
        chatService = new CommunityChatService(database, sessionManager);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    private void loadCurrentUser() {
        executorService.execute(() -> {
            try {
                int userId = sessionManager.getUserId();
                currentUser = database.userDao().getUserById(userId);
                
                runOnUiThread(() -> {
                    if (currentUser != null) {
                        updateBalanceDisplay();
                        updateMessageCostDisplay();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void setupRecyclerView() {
        messages = new ArrayList<>();
        adapter = new CommunityChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupMessageInput() {
        messageCostText.setText(getString(R.string.community_chat_cost, CommunityChatService.getMessageCost()));
        
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });
    }
    
    private void sendMessage(String message) {
        if (currentUser == null) {
            Toast.makeText(this, R.string.community_chat_user_error, Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (message.length() > CommunityChatService.getMaxMessageLength()) {
            Toast.makeText(this, getString(R.string.community_chat_message_too_long, CommunityChatService.getMaxMessageLength()), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!CommunityChatService.canSendMessage(currentUser)) {
            showInsufficientBalanceDialog();
            return;
        }
        
        boolean success = chatService.sendMessage(message, currentUser);
        if (success) {
            messageInput.setText("");
            Toast.makeText(this, R.string.community_chat_message_sent, Toast.LENGTH_SHORT).show();
            
            // Reload messages sau 1 giây để đảm bảo database đã được cập nhật
            new android.os.Handler().postDelayed(() -> {
                loadMessages();
            }, 1000);
        } else {
            Toast.makeText(this, R.string.community_chat_send_error, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showInsufficientBalanceDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.community_chat_insufficient_balance)
                .setMessage(getString(R.string.community_chat_insufficient_balance_message, CommunityChatService.getMessageCost()))
                .setPositiveButton(R.string.community_chat_deposit, (dialog, which) -> {
                    Intent intent = new Intent(this, WalletActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.community_chat_cancel, null)
                .show();
    }
    
    private void loadMessages() {
        executorService.execute(() -> {
            try {
                List<ChatCommunity> recentMessages = chatService.getRecentMessages(50);
                if (recentMessages != null) {
                    runOnUiThread(() -> {
                        messages.clear();
                        messages.addAll(recentMessages);
                        adapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) {
                            recyclerView.scrollToPosition(messages.size() - 1);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void updateBalanceDisplay() {
        if (currentUser != null) {
            balanceText.setText(getString(R.string.community_chat_balance, currentUser.getBalance()));
        }
    }
    
    private void updateMessageCostDisplay() {
        messageCostText.setText(getString(R.string.community_chat_cost, CommunityChatService.getMessageCost()));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUser(); // Refresh balance
        loadMessages(); // Refresh messages
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatService != null) {
            chatService.shutdown();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
