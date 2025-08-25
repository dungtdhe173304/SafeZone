package com.group5.safezone.view.component;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.group5.safezone.R;
import com.group5.safezone.model.entity.ChatCommunity;
import com.group5.safezone.service.CommunityChatService;
import com.group5.safezone.view.CommunityChatActivity;
import java.util.ArrayList;
import java.util.List;

public class CommunityChatHeaderView extends LinearLayout {
    private TextView messageText;
    private TextView userNameText;
    private View chatButton;
    private CommunityChatService chatService;
    private List<ChatCommunity> currentMessages;
    private int currentMessageIndex = 0;
    private boolean isAnimating = false;
    
    public CommunityChatHeaderView(Context context) {
        super(context);
        init(context);
    }
    
    public CommunityChatHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public CommunityChatHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        android.util.Log.d("CommunityChatHeaderView", "init() called with context: " + context.getClass().getSimpleName());
        
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.component_community_chat_header, this, true);
        android.util.Log.d("CommunityChatHeaderView", "Layout inflated successfully");
        
        messageText = findViewById(R.id.message_text);
        userNameText = findViewById(R.id.user_name);
        chatButton = findViewById(R.id.chat_button);
        
        android.util.Log.d("CommunityChatHeaderView", "Views found - messageText: " + (messageText != null ? "NOT NULL" : "NULL") + 
                          ", userNameText: " + (userNameText != null ? "NOT NULL" : "NULL") + 
                          ", chatButton: " + (chatButton != null ? "NOT NULL" : "NULL"));
        
        chatButton.setOnClickListener(v -> {
            android.util.Log.d("CommunityChatHeaderView", "Chat button clicked, opening CommunityChatActivity");
            Intent intent = new Intent(context, CommunityChatActivity.class);
            context.startActivity(intent);
        });
        
        // Hiển thị tin nhắn mặc định
        messageText.setText("Chào mừng đến với SafeZone! 💬");
        userNameText.setText("Hệ thống");
        android.util.Log.d("CommunityChatHeaderView", "Default message set: Chào mừng đến với SafeZone! 💬");
        
        // Bắt đầu animation sau khi view được layout
        post(() -> startMessageAnimation());
    }
    
    public void setChatService(CommunityChatService service) {
        android.util.Log.d("CommunityChatHeaderView", "setChatService called with service: " + (service != null ? "NOT NULL" : "NULL"));
        
        this.chatService = service;
        if (service != null) {
            android.util.Log.d("CommunityChatHeaderView", "Setting MessageDisplayListener");
            
            // Set listener trước khi gọi bất kỳ method nào khác
            service.setMessageDisplayListener(new CommunityChatService.MessageDisplayListener() {
                @Override
                public void onNewMessage(ChatCommunity message) {
                    android.util.Log.d("CommunityChatHeaderView", "New message: " + message.getMessage());
                     
                     // Thêm tin nhắn mới vào đầu danh sách
                     if (currentMessages == null) {
                         currentMessages = new ArrayList<>();
                     }
                     currentMessages.add(0, message);
                     
                     // Hiển thị tin nhắn mới ngay lập tức
                     currentMessageIndex = 0;
                     
                     // Dừng animation hiện tại nếu đang chạy
                     if (messageText != null) {
                         messageText.animate().cancel();
                     }
                     
                     // Hiển thị tin nhắn mới và bắt đầu animation
                     displayCurrentMessage();
                 }
                
                @Override
                public void onMessageDisplayed(ChatCommunity message) {
                    android.util.Log.d("CommunityChatHeaderView", "onMessageDisplayed called: " + message.getMessage());
                    // Tin nhắn đã hiển thị xong, chuyển sang tin nhắn tiếp theo
                    moveToNextMessage();
                }
            });
        }
    }
    
    public void setMessages(List<ChatCommunity> messages) {
        android.util.Log.d("CommunityChatHeaderView", "setMessages called with " + (messages != null ? messages.size() : 0) + " messages");
        
        this.currentMessages = messages;
        
        if (messages != null && !messages.isEmpty()) {
            android.util.Log.d("CommunityChatHeaderView", "Setting messages, first message: " + messages.get(0).getMessage());
            currentMessageIndex = 0;
            displayCurrentMessage();
        } else {
            android.util.Log.d("CommunityChatHeaderView", "No messages, showing default");
            // Hiển thị tin nhắn mặc định nếu không có tin nhắn
            messageText.setText("Chào mừng đến với SafeZone! 💬");
            userNameText.setText("Hệ thống");
            startMessageAnimation();
        }
    }
    
    private void startMessageAnimation() {
        if (messageText == null) {
            android.util.Log.e("CommunityChatHeaderView", "messageText is null, cannot start animation");
            return;
        }
        
        android.util.Log.d("CommunityChatHeaderView", "Starting message animation");
        
        // Đơn giản - chỉ chạy từ phải qua trái
        messageText.setTranslationX(500); // Bắt đầu từ bên phải
        messageText.animate()
                .translationX(-500) // Chạy về bên trái
                .setDuration(5000) // 5 giây
                .withEndAction(() -> {
                    android.util.Log.d("CommunityChatHeaderView", "Animation ended, moving to next message");
                    // Chuyển tin nhắn tiếp theo
                    moveToNextMessage();
                })
                .start();
    }
    
    private void displayCurrentMessage() {
        android.util.Log.d("CommunityChatHeaderView", "displayCurrentMessage called, currentMessages: " + (currentMessages != null ? currentMessages.size() : "null"));
        
        if (currentMessages == null || currentMessages.isEmpty()) {
            android.util.Log.d("CommunityChatHeaderView", "No messages, showing default");
            // Hiển thị tin nhắn mặc định
            messageText.setText("Chào mừng đến với SafeZone! 💬");
            userNameText.setText("Hệ thống");
            startMessageAnimation();
            return;
        }
        
        ChatCommunity message = currentMessages.get(currentMessageIndex);
        if (message != null) {
            android.util.Log.d("CommunityChatHeaderView", "Displaying message: " + message.getMessage() + " from user: " + message.getUserName());
            messageText.setText(message.getMessage());
            userNameText.setText(message.getUserName());
            
            // Bắt đầu animation đơn giản
            startMessageAnimation();
        } else {
            android.util.Log.e("CommunityChatHeaderView", "Message at index " + currentMessageIndex + " is null");
        }
    }
    
    private void moveToNextMessage() {
        if (currentMessages == null || currentMessages.isEmpty()) {
            // Nếu không có tin nhắn, hiển thị tin nhắn mặc định
            messageText.setText("Chào mừng đến với SafeZone! 💬");
            userNameText.setText("Hệ thống");
            startMessageAnimation();
            return;
        }
        
        currentMessageIndex = (currentMessageIndex + 1) % currentMessages.size();
        displayCurrentMessage();
    }
    
    public void pauseAnimation() {
        isAnimating = false;
        if (messageText != null) {
            messageText.animate().cancel();
        }
    }
    
    public void resumeAnimation() {
        isAnimating = true;
        startMessageAnimation();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pauseAnimation();
    }
}
