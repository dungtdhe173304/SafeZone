package com.group5.safezone.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.group5.safezone.R;

public final class NotificationPermissionUtil {

    private NotificationPermissionUtil() { }

    public static boolean hasPostNotificationsPermission(Context context) {
        if (Build.VERSION.SDK_INT < 33) return true;
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean areNotificationsEnabled(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static void requestPostNotifications(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, requestCode);
        }
    }

    public static void openAppNotificationSettings(Context context) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else {
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void promptIfNeeded(Activity activity, int requestCode) {
        boolean enabled = areNotificationsEnabled(activity);
        boolean hasPermission = hasPostNotificationsPermission(activity);

        if (hasPermission && enabled) return;

        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.notif_permission_title))
                .setMessage(activity.getString(R.string.notif_permission_msg))
                .setPositiveButton(activity.getString(R.string.allow), (d, w) -> {
                    if (!hasPermission && Build.VERSION.SDK_INT >= 33) {
                        requestPostNotifications(activity, requestCode);
                    } else {
                        openAppNotificationSettings(activity);
                    }
                })
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .show();
    }
}


