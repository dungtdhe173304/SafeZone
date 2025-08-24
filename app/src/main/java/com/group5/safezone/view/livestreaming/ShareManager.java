package com.group5.safezone.view.livestreaming;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;

/**
 * Manager class for handling live stream sharing functionality
 * Provides methods to share live stream invitations via various platforms
 */
public class ShareManager {

    private static final String SHARE_TITLE = "Join my Live Stream!";
    private static final String SHARE_SUBJECT = "Live Stream Invitation";

    /**
     * Share live stream invitation via text message
     * @param context Application context
     * @param liveID Unique identifier for the live stream
     * @param streamTitle Title of the live stream
     * @param hostName Name of the stream host
     */
    public static void shareLiveStreamInvitation(Context context, String liveID, String streamTitle, String hostName) {
        String shareText = createShareText(liveID, streamTitle, hostName);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, SHARE_SUBJECT);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        
        // Set title for chooser
        String chooserTitle = "Share Live Stream Invitation";
        
        // Check if there are apps that can handle this intent
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(shareIntent, chooserTitle));
        } else {
            Toast.makeText(context, "No apps available to share", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share live stream invitation via specific platform
     * @param context Application context
     * @param liveID Unique identifier for the live stream
     * @param streamTitle Title of the live stream
     * @param hostName Name of the stream host
     * @param platform Platform to share to (whatsapp, telegram, facebook, etc.)
     */
    public static void shareToSpecificPlatform(Context context, String liveID, String streamTitle, String hostName, String platform) {
        String shareText = createShareText(liveID, streamTitle, hostName);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, SHARE_SUBJECT);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        
        switch (platform.toLowerCase()) {
            case "whatsapp":
                shareIntent.setPackage("com.whatsapp");
                break;
            case "telegram":
                shareIntent.setPackage("org.telegram.messenger");
                break;
            case "facebook":
                shareIntent.setPackage("com.facebook.katana");
                break;
            case "messenger":
                shareIntent.setPackage("com.facebook.orca");
                break;
            case "instagram":
                shareIntent.setPackage("com.instagram.android");
                break;
            case "twitter":
                shareIntent.setPackage("com.twitter.android");
                break;
            case "email":
                shareIntent.setAction(Intent.ACTION_SENDTO);
                shareIntent.setData(Uri.parse("mailto:"));
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, SHARE_SUBJECT);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                break;
            case "sms":
                shareIntent.setAction(Intent.ACTION_SENDTO);
                shareIntent.setData(Uri.parse("smsto:"));
                shareIntent.putExtra("sms_body", shareText);
                break;
            default:
                // Use generic chooser
                context.startActivity(Intent.createChooser(shareIntent, "Share Live Stream"));
                return;
        }
        
        // Check if the specific app is installed
        if (isAppInstalled(context, shareIntent.getPackage())) {
            try {
                context.startActivity(shareIntent);
            } catch (Exception e) {
                Toast.makeText(context, "Error opening " + platform, Toast.LENGTH_SHORT).show();
                // Fallback to generic chooser
                shareIntent.setPackage(null);
                context.startActivity(Intent.createChooser(shareIntent, "Share Live Stream"));
            }
        } else {
            Toast.makeText(context, platform + " is not installed", Toast.LENGTH_SHORT).show();
            // Fallback to generic chooser
            shareIntent.setPackage(null);
            context.startActivity(Intent.createChooser(shareIntent, "Share Live Stream"));
        }
    }

    /**
     * Create shareable text for live stream invitation
     * @param liveID Unique identifier for the live stream
     * @param streamTitle Title of the live stream
     * @param hostName Name of the stream host
     * @return Formatted share text
     */
    private static String createShareText(String liveID, String streamTitle, String hostName) {
        StringBuilder shareText = new StringBuilder();
        shareText.append("🎥 ").append(streamTitle).append("\n\n");
        shareText.append("👤 Host: ").append(hostName).append("\n");
        shareText.append("🆔 Stream ID: ").append(liveID).append("\n\n");
        shareText.append("To join my live stream:\n");
        shareText.append("1. Open SafeZone app\n");
        shareText.append("2. Tap 'Join Live Stream'\n");
        shareText.append("3. Enter this Stream ID: ").append(liveID).append("\n");
        shareText.append("4. Tap 'Join Live Stream'\n\n");
        shareText.append("📱 Download SafeZone app to watch live streams!");
        
        return shareText.toString();
    }

    /**
     * Check if a specific app is installed
     * @param context Application context
     * @param packageName Package name of the app to check
     * @return True if app is installed, false otherwise
     */
    private static boolean isAppInstalled(Context context, String packageName) {
        if (packageName == null) return false;
        
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get list of available sharing platforms
     * @param context Application context
     * @return List of available sharing options
     */
    public static String[] getAvailablePlatforms(Context context) {
        return new String[]{
            "General Share",
            "WhatsApp",
            "Telegram", 
            "Facebook",
            "Messenger",
            "Instagram",
            "Twitter",
            "Email",
            "SMS"
        };
    }

    /**
     * Share live stream invitation with custom message
     * @param context Application context
     * @param liveID Unique identifier for the live stream
     * @param streamTitle Title of the live stream
     * @param hostName Name of the stream host
     * @param customMessage Custom message to include
     */
    public static void shareWithCustomMessage(Context context, String liveID, String streamTitle, String hostName, String customMessage) {
        String shareText = createShareText(liveID, streamTitle, hostName);
        
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            shareText = customMessage + "\n\n" + shareText;
        }
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, SHARE_SUBJECT);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Live Stream"));
    }
}
