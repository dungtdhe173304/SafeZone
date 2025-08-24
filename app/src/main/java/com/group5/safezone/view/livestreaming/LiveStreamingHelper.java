package com.group5.safezone.view.livestreaming;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import android.Manifest;

/**
 * Helper class for live streaming troubleshooting
 * Provides methods to check camera and microphone status
 */
public class LiveStreamingHelper {

    /**
     * Check if camera is available and working
     */
    public static boolean isCameraAvailable(Context context) {
        // Check permission first
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        // Check if device has camera
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return false;
        }

        // Try to open camera to check if it's working
        try {
            Camera camera = Camera.open();
            if (camera != null) {
                camera.release();
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * Check if microphone is available
     */
    public static boolean isMicrophoneAvailable(Context context) {
        // Check permission first
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        // Check if device has microphone
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            return false;
        }

        // Check audio manager
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager != null && audioManager.isMicrophoneMute() == false;
    }

    /**
     * Get troubleshooting tips for camera issues
     */
    public static String getCameraTroubleshootingTips() {
        StringBuilder tips = new StringBuilder();
        tips.append("🔍 Camera Troubleshooting Tips:\n\n");
        tips.append("1. ✅ Check if camera permission is granted\n");
        tips.append("2. 📱 Make sure no other app is using camera\n");
        tips.append("3. 🔄 Restart the app\n");
        tips.append("4. 📱 Restart your device\n");
        tips.append("5. 🧹 Clear app cache and data\n");
        tips.append("6. 📱 Check if camera is working in other apps\n");
        tips.append("7. 🔧 Update your device software\n");
        tips.append("8. 📱 Check if camera lens is covered\n");
        tips.append("9. 🔄 Try switching between front/back camera\n");
        tips.append("10. 📱 Check if camera app works normally\n\n");
        tips.append("💡 If problem persists, try using a different device");
        
        return tips.toString();
    }

    /**
     * Get troubleshooting tips for microphone issues
     */
    public static String getMicrophoneTroubleshootingTips() {
        StringBuilder tips = new StringBuilder();
        tips.append("🎤 Microphone Troubleshooting Tips:\n\n");
        tips.append("1. ✅ Check if microphone permission is granted\n");
        tips.append("2. 📱 Make sure microphone is not muted\n");
        tips.append("3. 🔄 Restart the app\n");
        tips.append("4. 📱 Restart your device\n");
        tips.append("5. 🧹 Clear app cache and data\n");
        tips.append("6. 📱 Check if microphone works in other apps\n");
        tips.append("7. 🔧 Update your device software\n");
        tips.append("8. 📱 Check if headphones are connected\n");
        tips.append("9. 🔄 Try switching audio output\n");
        tips.append("10. 📱 Check device volume settings\n\n");
        tips.append("💡 If problem persists, try using a different device");
        
        return tips.toString();
    }

    /**
     * Show camera status toast
     */
    public static void showCameraStatus(Context context) {
        if (isCameraAvailable(context)) {
            Toast.makeText(context, "✅ Camera is available and working", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "❌ Camera is not available or not working", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show microphone status toast
     */
    public static void showMicrophoneStatus(Context context) {
        if (isMicrophoneAvailable(context)) {
            Toast.makeText(context, "✅ Microphone is available and working", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "❌ Microphone is not available or not working", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check if all required permissions are granted
     */
    public static boolean areAllPermissionsGranted(Context context) {
        return isCameraAvailable(context) && isMicrophoneAvailable(context);
    }
}
