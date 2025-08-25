package com.group5.safezone.service;

import android.util.Log;
import com.group5.safezone.model.entity.ChatCommunity;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.Date;

public class CommunityChatService {
    private static final String TAG = "CommunityChatService";
    private static final int MAX_MESSAGE_LENGTH = 200;
    private static final double MESSAGE_COST = 5000.0; // 5,000 VND
    
    private AppDatabase database;
    private SessionManager sessionManager;
    private ExecutorService executorService;
    private LinkedBlockingQueue<ChatCommunity> messageQueue;
    private MessageDisplayListener displayListener;
    
    public interface MessageDisplayListener {
        void onNewMessage(ChatCommunity message);
        void onMessageDisplayed(ChatCommunity message);
    }
    
    public CommunityChatService(AppDatabase database, SessionManager sessionManager) {
        this.database = database;
        this.sessionManager = sessionManager;
        this.executorService = Executors.newSingleThreadExecutor();
        this.messageQueue = new LinkedBlockingQueue<>();
        
        Log.d(TAG, "CommunityChatService initialized successfully");
    }
    
    public boolean sendMessage(String message, User user) {
        // Kiểm tra độ dài tin nhắn
        if (message == null || message.trim().isEmpty() || message.length() > MAX_MESSAGE_LENGTH) {
            return false;
        }
        
        // Kiểm tra balance
        if (user.getBalance() < MESSAGE_COST) {
            return false;
        }
        
        // Tạo tin nhắn mới
        ChatCommunity chatMessage = new ChatCommunity();
        chatMessage.setMessage(message.trim());
        chatMessage.setUserId(user.getId());
        chatMessage.setUserName(user.getUserName());
        chatMessage.setCreatedAt(new Date());
        chatMessage.setUpdatedAt(new Date());
        chatMessage.setDisplayed(false);
        chatMessage.setDisplayOrder(0);
        
        // Lưu vào database trên background thread
        executorService.execute(() -> {
            try {
                database.chatCommunityDao().insert(chatMessage);
                user.setBalance(user.getBalance() - MESSAGE_COST);
                database.userDao().updateUser(user);
                
                Log.d(TAG, "Message sent successfully: " + message + " - Balance deducted: " + MESSAGE_COST);
                
                                 // Thông báo cho UI trên main thread
                 if (displayListener != null) {
                     new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                         displayListener.onNewMessage(chatMessage);
                     });
                 }
                 
                 // Thông báo message đã được lưu thành công
                 new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                     displayListener.onMessageDisplayed(chatMessage);
                 });
            } catch (Exception e) {
                Log.e(TAG, "Error sending message", e);
            }
        });
        
        return true;
    }
    

    
    public List<ChatCommunity> getRecentMessages(int limit) {
        try {
            Log.d(TAG, "Getting recent messages, limit: " + limit);
            // Sử dụng background thread để lấy messages
            List<ChatCommunity> messages = database.chatCommunityDao().getRecentMessages(limit);
            
            Log.d(TAG, "Retrieved " + (messages != null ? messages.size() : 0) + " messages");
            return messages;
        } catch (Exception e) {
            Log.e(TAG, "Error getting recent messages", e);
            return null;
        }
    }
    
    public void setMessageDisplayListener(MessageDisplayListener listener) {
        this.displayListener = listener;
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    public static boolean canSendMessage(User user) {
        return user.getBalance() >= MESSAGE_COST;
    }
    
    public static double getMessageCost() {
        return MESSAGE_COST;
    }
    
    public static int getMaxMessageLength() {
        return MAX_MESSAGE_LENGTH;
    }
}
