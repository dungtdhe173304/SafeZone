package com.group5.safezone.config;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.group5.safezone.R;
import com.group5.safezone.view.Wallet.WalletActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.NumberFormat;
import java.util.Locale;

public final class AppNotificationHelper {

    private static final String CHANNEL_ID = "safezone_general";
    private static final String CHANNEL_NAME = "SafeZone";
    private static final String CHANNEL_DESC = "Thông báo từ SafeZone";

    private AppNotificationHelper() { }

    private static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription(CHANNEL_DESC);
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showDepositSuccess(Context context, double amount, int relatedId) {
        ensureChannel(context);

        Intent intent = new Intent(context, WalletActivity.class);
        intent.putExtra("open_from_notification", true);
        intent.putExtra("notification_type", "DEPOSIT_SUCCESS");
        intent.putExtra("related_id", relatedId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1001,
                intent,
                Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String title = context.getString(R.string.deposit_success);
        String content = "Bạn đã nạp thành công " + nf.format(amount);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_wallet)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(2001, builder.build());

        // thông báo cho UI cập nhật badge
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NotificationEvents.ACTION_UPDATED));
    }

    // Backward-compatible wrapper if needed
    public static void showDepositSuccess(Context context, double amount) {
        showDepositSuccess(context, amount, -1);
    }
}


