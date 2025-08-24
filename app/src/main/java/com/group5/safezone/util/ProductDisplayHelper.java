package com.group5.safezone.util;

import java.util.Date;

public class ProductDisplayHelper {
    
    public static String formatTimeAgo(Date date) {
        if (date == null) {
            return "Just now";
        }
        
        long timeInMillis = date.getTime();
        long currentTime = System.currentTimeMillis();
        long diffInMillis = currentTime - timeInMillis;
        
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;
        
        if (diffInDays > 0) {
            return diffInDays + " day" + (diffInDays > 1 ? "s" : "") + " ago";
        } else if (diffInHours > 0) {
            return diffInHours + " hour" + (diffInHours > 1 ? "s" : "") + " ago";
        } else if (diffInMinutes > 0) {
            return diffInMinutes + " minute" + (diffInMinutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }
    
    public static String formatUsername(int userId) {
        // For now, just return "User " + userId
        // In the future, this could fetch from User database
        return "User " + userId;
    }
    
    public static String formatPrice(Double price) {
        if (price == null) {
            return "$0.00";
        }
        return String.format("$%.2f", price);
    }
    
    public static String formatViews(Integer views) {
        if (views == null) {
            return "👁 0 views";
        }
        return "👁 " + views + " view" + (views != 1 ? "s" : "");
    }

    public static String formatFee(Double fee) {
        if (fee == null) {
            return "Phí: Chưa xác định";
        }
        
        if (fee == 1.0) {
            return "Phí: Người bán chịu";
        } else if (fee == 2.0) {
            return "Phí: Người mua chịu";
        } else {
            return "Phí: " + fee;
        }
    }
}
