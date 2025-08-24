package com.group5.safezone.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.group5.safezone.R;

public class ChatNotificationService extends Service {
    
    private WindowManager windowManager;
    private View popupView;
    
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String senderName = intent.getStringExtra("sender_name");
            String message = intent.getStringExtra("message");
            
            if (senderName != null && message != null) {
                showChatPopup(senderName, message);
            }
        }
        return START_NOT_STICKY;
    }
    
    private void showChatPopup(String senderName, String message) {
        try {
            // Tạo popup view
            popupView = LayoutInflater.from(this).inflate(R.layout.popup_chat_notification, null);
            
            // Set text
            TextView tvSenderName = popupView.findViewById(R.id.tvSenderName);
            TextView tvMessage = popupView.findViewById(R.id.tvMessage);
            
            tvSenderName.setText(senderName);
            tvMessage.setText(message);
            
            // Window parameters
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT
            );
            
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = 100; // Khoảng cách từ top
            
            // Add view to window
            windowManager.addView(popupView, params);
            
            // Auto hide sau 5 giây
            popupView.postDelayed(() -> {
                try {
                    if (popupView != null && popupView.getParent() != null) {
                        windowManager.removeView(popupView);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 5000);
            
            // Click để ẩn popup
            popupView.setOnClickListener(v -> {
                try {
                    if (popupView != null && popupView.getParent() != null) {
                        windowManager.removeView(popupView);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: hiển thị Toast nếu không thể hiển thị popup
            Toast.makeText(this, "Tin nhắn mới từ " + senderName + ": " + message, Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cleanup popup nếu còn
        if (popupView != null && popupView.getParent() != null) {
            try {
                windowManager.removeView(popupView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
