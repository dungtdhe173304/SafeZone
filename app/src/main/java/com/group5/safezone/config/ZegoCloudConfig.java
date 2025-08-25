package com.group5.safezone.config;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.zegocloud.zimkit.services.ZIMKit;
import com.group5.safezone.Constant.Chatbox.ConstantKey;

/**
 * ZEGOCLOUD Configuration Helper
 * Xử lý lỗi avatar và các vấn đề khác từ ZEGOCLOUD SDK
 */
public class ZegoCloudConfig {
    
    private static final String TAG = "ZegoCloudConfig";
    private static boolean isInitialized = false;
    
    /**
     * Khởi tạo ZEGOCLOUD SDK một cách an toàn
     */
    public static boolean initSafely(Context context) {
        if (isInitialized) {
            Log.d(TAG, "ZEGOCLOUD SDK already initialized");
            return true;
        }
        
        try {
            Log.d(TAG, "Initializing ZEGOCLOUD SDK safely...");
            
            // Khởi tạo với try-catch - cần Application context
            Application app = (Application) context.getApplicationContext();
            ZIMKit.initWith(app, ConstantKey.appID, ConstantKey.appSign);
            ZIMKit.initNotifications();
            
            isInitialized = true;
            Log.d(TAG, "ZEGOCLOUD SDK initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ZEGOCLOUD SDK: " + e.getMessage(), e);
            
            // Thử khởi tạo lại với cấu hình đơn giản
            try {
                Log.d(TAG, "Attempting fallback initialization...");
                Application app = (Application) context.getApplicationContext();
                ZIMKit.initWith(app, ConstantKey.appID, ConstantKey.appSign);
                isInitialized = true;
                Log.d(TAG, "ZEGOCLOUD SDK initialized with fallback");
                return true;
            } catch (Exception fallbackEx) {
                Log.e(TAG, "Fallback initialization also failed: " + fallbackEx.getMessage(), fallbackEx);
                return false;
            }
        }
    }
    
    /**
     * Kiểm tra xem ZEGOCLOUD có hoạt động không
     */
    public static boolean isWorking() {
        return isInitialized;
    }
    
    /**
     * Reset trạng thái khởi tạo (cho testing)
     */
    public static void reset() {
        isInitialized = false;
    }
}
