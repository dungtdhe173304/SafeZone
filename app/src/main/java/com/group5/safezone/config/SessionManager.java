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
        editor.putLong(KEY_BALANCE, user.getBalance() != null ? user.getBalance().longValue() : 0L);
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
        try {
            // Try to get as float first
            return pref.getFloat(KEY_BALANCE, 0f);
        } catch (ClassCastException e) {
            // If float fails, try to get as long and convert
            try {
                long longBalance = pref.getLong(KEY_BALANCE, 0L);
                return (double) longBalance;
            } catch (ClassCastException e2) {
                // If long also fails, try to get as int and convert
                try {
                    int intBalance = pref.getInt(KEY_BALANCE, 0);
                    return (double) intBalance;
                } catch (ClassCastException e3) {
                    // If all fail, return 0
                    return 0.0;
                }
            }
        }
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
        // Store as long to avoid precision issues with float
        editor.putLong(KEY_BALANCE, (long) newBalance);
        editor.apply();
    }

    public void updateVerifyStatus(boolean isVerified) {
        editor.putBoolean(KEY_IS_VERIFY, isVerified);
        editor.apply();
    }
}
