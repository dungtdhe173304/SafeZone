package com.group5.safezone.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.group5.safezone.model.entity.User;

public class SessionManager {
    private static final String PREF_NAME = "SafeZoneSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_STATUS = "status";
    private static final String KEY_BALANCE = "balance";
    private static final String KEY_IS_VERIFY = "isVerify";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getUserName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_ROLE, user.getRole());
        editor.putString(KEY_STATUS, user.getStatus());
        editor.putFloat(KEY_BALANCE, user.getBalance() != null ? user.getBalance().floatValue() : 0f);
        editor.putBoolean(KEY_IS_VERIFY, user.getIsVerify() != null ? user.getIsVerify() : false);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, null);
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getUserRole() {
        return pref.getString(KEY_ROLE, "USER");
    }

    public String getUserStatus() {
        return pref.getString(KEY_STATUS, "PENDING");
    }

    public double getBalance() {
        return pref.getFloat(KEY_BALANCE, 0f);
    }

    public boolean isVerified() {
        return pref.getBoolean(KEY_IS_VERIFY, false);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getUserRole());
    }

    public boolean isUser() {
        return "USER".equals(getUserRole());
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public void updateBalance(double newBalance) {
        editor.putFloat(KEY_BALANCE, (float) newBalance);
        editor.apply();
    }

    public void updateVerifyStatus(boolean isVerified) {
        editor.putBoolean(KEY_IS_VERIFY, isVerified);
        editor.apply();
    }
}
