package com.group5.safezone.view.chat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Import Log class
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.group5.safezone.R;
import com.group5.safezone.adapter.UserSearchAdapter;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.repository.UserRepository;
import com.zegocloud.zimkit.common.ZIMKitRouter;
import com.zegocloud.zimkit.common.enums.ZIMKitConversationType;
import com.zegocloud.zimkit.services.ZIMKit;

import im.zego.zim.enums.ZIMErrorCode;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity"; // Tag cho Logcat
    private Toolbar toolbar;
    private FloatingActionButton actionButton;
    private SessionManager sessionManager;
    private UserRepository userRepository;
    private ExecutorService executorService;
    private User selectedUser;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        
        userRepository = new UserRepository(getApplication());
        executorService = Executors.newFixedThreadPool(2);
        
        initViews();
        setupToolbar();
        connectCurrentUser();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        actionButton = findViewById(R.id.floating_btn);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tin nhắn");
        }
    }

    private void connectCurrentUser() {
        String userId = sessionManager.getUserName();
        String userName = sessionManager.getUserName();
        // Giữ nguyên là null cho avatarUrl để ZEGO hoàn toàn bỏ qua
        String avatarUrl = "https://i.ytimg.com/vi/-Om9cVZeZ58/maxresdefault.jpg?sqp=-oaymwEmCIAKENAF8quKqQMa8AEB-AH-CYAC0AWKAgwIABABGH8gIigjMA8=&rs=AOn4CLBlJZt4vMRdBt-6BETpatSyTQ5ZEA";
        
        Log.d(TAG, "Attempting to connect current user to ZIMKit:");
        Log.d(TAG, "  userId: " + userId);
        Log.d(TAG, "  userName: " + userName);
        Log.d(TAG, "  avatarUrl: " + (avatarUrl == null ? "null" : avatarUrl)); // Log giá trị avatarUrl

        if (userId != null && !userId.equals("0")) {
            try {
                ZIMKit.connectUser(userId, userName, avatarUrl, errorInfo -> {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        runOnUiThread(() -> {
                            Toast.makeText(ChatActivity.this, "Đã kết nối chat thành công", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "ZIMKit connection successful for user: " + userName);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ChatActivity.this, "Lỗi kết nối chat: " + errorInfo.message + " (Code: " + errorInfo.code + ")", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "ZIMKit connection error for user " + userName + ": " + errorInfo.message + " (Code: " + errorInfo.code + ")");
                        });
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception during ZIMKit connectUser for current user: " + e.getMessage(), e);
                Toast.makeText(this, "Không thể kết nối chat do lỗi hệ thống", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Không thể lấy thông tin người dùng để kết nối chat", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Current userId is null or '0'. Cannot connect to ZIMKit.");
            finish();
        }
    }

    private void setupClickListeners() {
        actionButton.setOnClickListener(v -> showPopupMenu());
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, actionButton);
        popupMenu.getMenuInflater().inflate(R.menu.chat_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.new_chat) {
                showNewChatDialog();
                return true;
            }
            if (menuItem.getItemId() == R.id.logout_chat) {
                logoutFromChat();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showNewChatDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_chat, null);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);
        builder.setCancelable(true);
        
        AlertDialog dialog = builder.create();
        
        EditText etUserSearch = dialogView.findViewById(R.id.etUserSearch);
        RecyclerView rvUserList = dialogView.findViewById(R.id.rvUserList);
        View userListContainer = dialogView.findViewById(R.id.userListContainer);
        View selectedUserContainer = dialogView.findViewById(R.id.selectedUserContainer);
        TextView tvSelectedUserName = dialogView.findViewById(R.id.tvSelectedUserName);
        TextView tvSelectedUserEmail = dialogView.findViewById(R.id.tvSelectedUserEmail);
        View btnRemoveSelection = dialogView.findViewById(R.id.btnRemoveSelection);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnStartChat = dialogView.findViewById(R.id.btnStartChat);
        
        btnStartChat.setEnabled(false);
        
        UserSearchAdapter adapter = new UserSearchAdapter(user -> {
            selectedUser = user;
            selectedUserContainer.setVisibility(View.VISIBLE);
            userListContainer.setVisibility(View.GONE);
            tvSelectedUserName.setText(user.getUserName());
            tvSelectedUserEmail.setText(user.getEmail());
            btnStartChat.setEnabled(true);
            etUserSearch.setText("");
            Log.d(TAG, "User selected: " + user.getUserName() + " (ID: " + user.getId() + ")");
        });
        
        rvUserList.setLayoutManager(new LinearLayoutManager(this));
        rvUserList.setAdapter(adapter);
        
        etUserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchUsers(query, adapter, userListContainer, btnStartChat);
                } else {
                    adapter.clearUsers();
                    userListContainer.setVisibility(View.GONE);
                    if (selectedUser == null) {
                        btnStartChat.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        btnRemoveSelection.setOnClickListener(v -> {
            selectedUser = null;
            selectedUserContainer.setVisibility(View.GONE);
            btnStartChat.setEnabled(false);
            Log.d(TAG, "Selected user removed.");
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnStartChat.setOnClickListener(v -> {
            if (selectedUser != null) {
                try {
                    Log.d(TAG, "Attempting to register selected user " + selectedUser.getUserName() + " to ZEGO...");
                    
                    registerUserToZEGO(selectedUser, () -> {
                        try {
                            Log.d(TAG, "Navigating to ZIMKit message activity for user: " + selectedUser.getUserName());
                            ZIMKitRouter.toMessageActivity(
                                ChatActivity.this,
                                selectedUser.getUserName(),
                                ZIMKitConversationType.ZIMKitConversationTypePeer
                            );
                            dialog.dismiss();
                            Toast.makeText(ChatActivity.this, "Bắt đầu chat với " + selectedUser.getUserName(), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error navigating to message activity: " + e.getMessage(), e);
                            Toast.makeText(ChatActivity.this, "Lỗi khi mở chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error starting chat process: " + e.getMessage(), e);
                    Toast.makeText(ChatActivity.this, "Lỗi khi bắt đầu chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ChatActivity.this, "Vui lòng chọn người dùng", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Attempted to start chat without selecting a user.");
            }
        });
        
        dialog.show();
    }

    private void logoutFromChat() {
        ZIMKit.disconnectUser();
        Toast.makeText(this, "Đã đăng xuất khỏi chat", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "User logged out from ZIMKit.");
        finish();
    }
    
    private void searchUsers(String query, UserSearchAdapter adapter, View userListContainer, MaterialButton btnStartChat) {
        executorService.execute(() -> {
            try {
                List<User> allUsers = userRepository.searchUsersByUsernameOrEmail(query);
                
                List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getId() != currentUserId)
                    .collect(Collectors.toList());
                
                runOnUiThread(() -> {
                    if (filteredUsers != null && !filteredUsers.isEmpty()) {
                        adapter.updateUsers(filteredUsers);
                        userListContainer.setVisibility(View.VISIBLE);
                        btnStartChat.setEnabled(selectedUser != null);
                        Log.d(TAG, "Found " + filteredUsers.size() + " users for query: " + query);
                    } else {
                        adapter.clearUsers();
                        userListContainer.setVisibility(View.GONE);
                        btnStartChat.setEnabled(false);
                        if (allUsers != null && !allUsers.isEmpty()) {
                            Toast.makeText(ChatActivity.this, "Không tìm thấy người dùng khác để chat", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No other users found for chat for query: " + query);
                        } else {
                            Toast.makeText(ChatActivity.this, "Không tìm thấy người dùng nào", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No users found at all for query: " + query);
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Lỗi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error searching users for query '" + query + "': " + e.getMessage(), e);
                    adapter.clearUsers();
                    userListContainer.setVisibility(View.GONE);
                    btnStartChat.setEnabled(false);
                });
            }
        });
    }

    private void registerUserToZEGO(User user, Runnable onSuccess) {
        String userId = String.valueOf(user.getId());
        String userName = user.getUserName();
        // Giữ nguyên là null cho avatarUrl
        String avatarUrl = "https://i.ytimg.com/vi/-Om9cVZeZ58/maxresdefault.jpg?sqp=-oaymwEmCIAKENAF8quKqQMa8AEB-AH-CYAC0AWKAgwIABABGH8gIigjMA8=&rs=AOn4CLBlJZt4vMRdBt-6BETpatSyTQ5ZEA"; 
        
        Log.d(TAG, "Attempting to register user " + userName + " (ID: " + userId + ") to ZEGO:");
        Log.d(TAG, "  userId: " + userId);
        Log.d(TAG, "  userName: " + userName);
        Log.d(TAG, "  avatarUrl: " + (avatarUrl == null ? "null" : avatarUrl));

        try {
            ZIMKit.connectUser(userId, userName, avatarUrl, errorInfo -> {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    Log.d(TAG, "User " + userName + " successfully registered/connected to ZEGO.");
                    runOnUiThread(onSuccess);
                } else {
                    Log.e(TAG, "Failed to register/connect user " + userName + " to ZEGO: " + errorInfo.message + " (Code: " + errorInfo.code + ")");
                    // Fallback: thử kết nối với userId và userName đơn giản nếu lỗi
                    try {
                        Log.d(TAG, "Attempting fallback connection for user " + userName + " (no avatarUrl)...");
                        ZIMKit.connectUser(userId, userName, null, fallbackErrorInfo -> { 
                            if (fallbackErrorInfo.code == ZIMErrorCode.SUCCESS) {
                                Log.d(TAG, "Fallback connection successful for user " + userName);
                                runOnUiThread(onSuccess);
                            } else {
                                Log.e(TAG, "Fallback connection failed for user " + userName + ": " + fallbackErrorInfo.message + " (Code: " + fallbackErrorInfo.code + ")");
                                runOnUiThread(() -> {
                                    Toast.makeText(ChatActivity.this, "Không thể kết nối ZEGO: " + fallbackErrorInfo.message, Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    } catch (Exception fallbackEx) {
                        Log.e(TAG, "Exception during ZIMKit fallback connectUser: " + fallbackEx.getMessage(), fallbackEx);
                        runOnUiThread(() -> {
                            Toast.makeText(ChatActivity.this, "ZEGO không thể kết nối", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception when attempting to register user to ZEGO: " + e.getMessage(), e);
            runOnUiThread(() -> {
                Toast.makeText(ChatActivity.this, "Không thể đăng ký user vào ZEGO", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "Executor service shut down.");
        }
    }
}
