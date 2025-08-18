package com.group5.safezone.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.group5.safezone.view.auth.LoginActivity;

public class AuthInterceptor {

    public static boolean checkAuthentication(Context context) {
        SessionManager sessionManager = new SessionManager(context);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin(context);
            return false;
        }

        return true;
    }

    public static boolean checkAdminPermission(Context context) {
        SessionManager sessionManager = new SessionManager(context);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin(context);
            return false;
        }

        if (!sessionManager.isAdmin()) {
            Toast.makeText(context, "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public static boolean checkUserPermission(Context context) {
        SessionManager sessionManager = new SessionManager(context);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin(context);
            return false;
        }

        if (!sessionManager.isUser() && !sessionManager.isAdmin()) {
            Toast.makeText(context, "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public static boolean checkAccountStatus(Context context) {
        SessionManager sessionManager = new SessionManager(context);

        String status = sessionManager.getUserStatus();

        if ("BANNED".equals(status)) {
            Toast.makeText(context, "Tài khoản của bạn đã bị khóa", Toast.LENGTH_LONG).show();
            sessionManager.logout();
            redirectToLogin(context);
            return false;
        }

        if ("INACTIVE".equals(status)) {
            Toast.makeText(context, "Tài khoản chưa được kích hoạt", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private static void redirectToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }
}
