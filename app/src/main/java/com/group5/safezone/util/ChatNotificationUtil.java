package com.group5.safezone.util;

import android.content.Context;
import android.content.Intent;

import com.group5.safezone.service.ChatNotificationService;

public class ChatNotificationUtil {
    
    /**
     * Hiển thị popup thông báo tin nhắn mới
     * @param context Context của app
     * @param senderName Tên người gửi tin nhắn
     * @param message Nội dung tin nhắn
     */
    public static void showChatNotification(Context context, String senderName, String message) {
        try {
            Intent intent = new Intent(context, ChatNotificationService.class);
            intent.putExtra("sender_name", senderName);
            intent.putExtra("message", message);
            
            // Start service để hiển thị popup
            context.startService(intent);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Hiển thị popup thông báo tin nhắn mới với callback
     * @param context Context của app
     * @param senderName Tên người gửi tin nhắn
     * @param message Nội dung tin nhắn
     * @param onNotificationShown Callback khi thông báo được hiển thị
     */
    public static void showChatNotification(Context context, String senderName, String message, Runnable onNotificationShown) {
        try {
            Intent intent = new Intent(context, ChatNotificationService.class);
            intent.putExtra("sender_name", senderName);
            intent.putExtra("message", message);
            
            // Start service để hiển thị popup
            context.startService(intent);
            
            // Gọi callback nếu có
            if (onNotificationShown != null) {
                onNotificationShown.run();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
